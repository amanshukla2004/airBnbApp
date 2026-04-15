package com.aman.project.airBnbApp.service;

import static com.aman.project.airBnbApp.util.AppUtils.getCurrentUser;

import com.aman.project.airBnbApp.dto.BookingDto;
import com.aman.project.airBnbApp.dto.BookingRequest;
import com.aman.project.airBnbApp.dto.GuestDto;
import com.aman.project.airBnbApp.dto.HotelReportDto;
import com.aman.project.airBnbApp.entity.*;
import com.aman.project.airBnbApp.entity.enums.BookingStatus;
import com.aman.project.airBnbApp.exception.ResourceNotFoundException;
import com.aman.project.airBnbApp.exception.UnAuthorisedException;
import com.aman.project.airBnbApp.repository.*;
import com.aman.project.airBnbApp.strategy.PricingService;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

	private final GuestRepository guestRepository;

	private final BookingRepository bookingRepository;
	private final RoomRepository roomRepository;
	private final HotelRepository hotelRepository;
	private final InventoryRepository inventoryRepository;
	private final ModelMapper modelMapper;
	private final CheckoutService checkoutService;
	private final PricingService pricingService;

	@Value("${frontend.url}")
	private String frontendUrl;

	@Override
	@Transactional
	public BookingDto initialiseBooking(BookingRequest bookingRequest) {
		log.info(
				"initialise Booking request for hotel : {}," + " rooms: {}, date {} to {}",
				bookingRequest.getHotelId(),
				bookingRequest.getRoomId(),
				bookingRequest.getCheckInDate(),
				bookingRequest.getCheckOutDate());

		Hotel hotel = hotelRepository
				.findById(bookingRequest.getHotelId())
				.orElseThrow(
						() -> new ResourceNotFoundException("Hotel not found with id" + bookingRequest.getHotelId()));
		Room room = roomRepository
				.findById(bookingRequest.getRoomId())
				.orElseThrow(
						() -> new ResourceNotFoundException("Room not found with id" + bookingRequest.getRoomId()));
		//
		List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(
				room.getId(),
				bookingRequest.getCheckInDate(),
				bookingRequest.getCheckOutDate(),
				bookingRequest.getRoomsCount());

		long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate()) + 1;

		if (inventoryList.size() != daysCount) {
			throw new IllegalStateException("Room is not available anymore");
		}
		// Reserve the room/ update the booked count of inventories
		inventoryRepository.initBooking(
				room.getId(),
				bookingRequest.getCheckInDate(),
				bookingRequest.getCheckOutDate(),
				bookingRequest.getRoomsCount());

		// Create the Booking
		BigDecimal priceForOneRoom = pricingService.calculateTotalPricing(inventoryList);
		BigDecimal totalPrice = priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequest.getRoomsCount()));

		Booking booking = Booking
				.builder()
				.bookingStatus(BookingStatus.RESERVED)
				.hotel(hotel)
				.room(room)
				.checkInDate(bookingRequest.getCheckInDate())
				.checkOutDate(bookingRequest.getCheckOutDate())
				.user(getCurrentUser())
				.roomCount(bookingRequest.getRoomsCount())
				.amount(totalPrice)
				.build();

		booking = bookingRepository.save(booking);
		log.info("Booking has been saved successfully : {}", booking);
		return modelMapper.map(booking, BookingDto.class);
	}

	@Transactional
	@Override
	public BookingDto addGuests(Long bookingId, List<Long> guestIds) {
		log.info("Adding Guests for Booking for id : {}", bookingId);
		// 1. Fetch Booking
		Booking booking = bookingRepository
				.findById(bookingId)
				.orElseThrow(() -> new ResourceNotFoundException("Booking not found with id" + bookingId));

		// 2. Get User Safely
		User user = getCurrentUser();
		if (!user.getId().equals(booking.getUser().getId())) {
			throw new UnAuthorisedException("Booking does not belong to this user with ID: " + user.getId());
		}

		if (hasBookingExpired(booking)) {
			throw new IllegalStateException("Booking has expired");
		}
		if (booking.getBookingStatus() != BookingStatus.RESERVED) {
			throw new IllegalStateException("Booking is not under reserved state, cannot add guests");
		}

		List<Guest> guests = guestRepository.findAllById(guestIds);
		if (guests.size() != guestIds.size()) {
			throw new ResourceNotFoundException("Some guests were not found");
		}

		// Validate that all guests belong to this user
		for (Guest guest : guests) {
			if (!guest.getUser().getId().equals(user.getId())) {
				throw new UnAuthorisedException("Guest with ID " + guest.getId() + " does not belong to you");
			}
		}

		booking.getGuests().addAll(guests);

		booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
		booking = bookingRepository.save(booking);

		return modelMapper.map(booking, BookingDto.class);
	}

	@Override
	@Transactional
	public String initiatePayments(Long bookingId) {
		Booking booking = bookingRepository
				.findById(bookingId)
				.orElseThrow(() -> new ResourceNotFoundException("Booking not found with id" + bookingId));
		User user = getCurrentUser();
		// ❌ OLD: if (!user.equals(booking.getUser())) i added
		// ✅ NEW: Compare IDs (Long vs Long)
		if (!user.getId().equals(booking.getUser().getId())) {
			throw new UnAuthorisedException("Booking does not belong to this user with ID: " + user.getId());
		}
		if (hasBookingExpired(booking)) {
			throw new IllegalStateException("Booking has expired");
		}
		// TODO : In frontend make the route payments/success and payments/failure
		// pole our backend every 3 sec to check the booking status
		String sessionUrl = checkoutService.getCheckoutSession(
				booking,
				frontendUrl + "/payments/success",
				frontendUrl + "/payments/failure");
		booking.setBookingStatus(BookingStatus.PAYMENTS_PENDING);
		bookingRepository.save(booking);
		return sessionUrl;
	}

	// for stripe -> used in WebhookController
	@Override
	@Transactional
	public void capturePayment(Event event) {
		log.info("Processing Stripe event: {}", event.getType());

		// 1. Restrict to checkout.session.completed for maximum reliability
		if (!"checkout.session.completed".equals(event.getType())) {
			log.info("Skipping event type: {} (not checkout.session.completed)", event.getType());
			return;
		}

		try {
			// 2. Safe Deserialization of the Session object
			Session session = (Session) event.getDataObjectDeserializer()
					.getObject()
					.orElseThrow(() -> new IllegalStateException("Failed to deserialize Stripe Session object"));

			// 3. Robust Metadata Extraction
			if (session.getMetadata() == null || !session.getMetadata().containsKey("bookingId")) {
				log.warn("Stripe Session {} missing bookingId in metadata. Skipping.", session.getId());
				return;
			}

			Long bookingId = Long.parseLong(session.getMetadata().get("bookingId"));

			// 4. Idempotent Confirmation
			confirmBooking(bookingId);

		} catch (Exception e) {
			log.error("Internal error while processing Stripe webhook {}: {}", event.getId(), e.getMessage(), e);
			// We do NOT throw a RuntimeException here.
			// We log it and let the controller return 200 to prevent Stripe from infinite
			// retries
			// if the error is data-specific.
		}
	}

	private void confirmBooking(Long bookingId) {
		Booking booking = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new ResourceNotFoundException("Booking not found with id " + bookingId));

		if (booking.getBookingStatus() == BookingStatus.CONFIRMED) {
			log.info("Booking {} is already confirmed. Skipping.", bookingId);
			return;
		}

		booking.setBookingStatus(BookingStatus.CONFIRMED);
		bookingRepository.save(booking);

		inventoryRepository.findAndLockReservedInventory(
				booking.getRoom().getId(),
				booking.getCheckInDate(),
				booking.getCheckOutDate(),
				booking.getRoomCount());

		inventoryRepository.confirmBooking(
				booking.getRoom().getId(),
				booking.getCheckInDate(),
				booking.getCheckOutDate(),
				booking.getRoomCount());

		log.info("Booking confirmed successfully for ID: {}", bookingId);
	}

	@Transactional
	@Override
	public void cancelBooking(Long bookingId) {
		log.info("Starting the cancel of booking : {}", bookingId);
		Booking booking = bookingRepository
				.findById(bookingId)
				.orElseThrow(() -> new ResourceNotFoundException("Booking not found with id" + bookingId));
		User user = getCurrentUser();
		// ❌ OLD: if (!user.equals(booking.getUser())) i added
		// ✅ NEW: Compare IDs (Long vs Long)
		if (!user.getId().equals(booking.getUser().getId())) {
			throw new UnAuthorisedException("Booking does not belong to this user with ID: " + user.getId());
		}
		if ((booking.getBookingStatus() != BookingStatus.CONFIRMED)) {
			throw new IllegalStateException("Only confirmed booking can be cancelled");
		}
		booking.setBookingStatus(BookingStatus.CANCELLED);
		bookingRepository.save(booking);
		///
		inventoryRepository.findAndLockReservedInventory(
				booking.getRoom().getId(),
				booking.getCheckInDate(),
				booking.getCheckOutDate(),
				booking.getRoomCount());

		inventoryRepository.cancelBooking(
				booking.getRoom().getId(),
				booking.getCheckInDate(),
				booking.getCheckOutDate(),
				booking.getRoomCount());
		// handle the refund
		log.info("Starting the handling of booking : {}", bookingId);
		try {
			Session session = Session.retrieve(booking.getPaymentSessionId());
			RefundCreateParams refundParams = RefundCreateParams
					.builder()
					.setPaymentIntent(session.getPaymentIntent())
					.build();
			Refund.create(refundParams);
			log.info("done with the cancel of booking : {}", bookingId);
		} catch (StripeException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getBookingStatus(Long bookingId) {
		Booking booking = bookingRepository
				.findById(bookingId)
				.orElseThrow(() -> new ResourceNotFoundException("Booking not found with id" + bookingId));
		User user = getCurrentUser();
		// ❌ OLD: if (!user.equals(booking.getUser())) i added
		// ✅ NEW: Compare IDs (Long vs Long)
		if (!user.getId().equals(booking.getUser().getId())) {
			throw new UnAuthorisedException("Booking does not belong to this user with ID: " + user.getId());
		}
		return booking.getBookingStatus().name();
	}

	@Override
	public List<BookingDto> getAllBookingByHotelId(Long hotelId) {
		Hotel hotel = hotelRepository
				.findById(hotelId)
				.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id" + hotelId));

		log.info("Getting all bookings for the hotel with ID: {} ", hotelId);

		User user = getCurrentUser();
		if (!user.getId().equals(hotel.getOwner().getId())) {
			throw new AccessDeniedException("You are not the owner of hotel with id: " + hotelId);
		}
		List<Booking> bookings = bookingRepository.findByHotel(hotel);

		return bookings
				.stream()
				.map(element -> modelMapper.map(element, BookingDto.class))
				.collect(Collectors.toList());
	}

	@Override
	public HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate) {
		Hotel hotel = hotelRepository
				.findById(hotelId)
				.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id" + hotelId));

		log.info("Generating report for hotel with ID: {} ", hotelId);

		User user = getCurrentUser();
		if (!user.getId().equals(hotel.getOwner().getId())) {
			throw new AccessDeniedException("You are not the owner of hotel with id: " + hotelId);
		}
		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

		List<Booking> bookings = bookingRepository.findByHotelAndCreatedAtBetween(hotel, startDateTime, endDateTime);

		Long totalConfirmBooking = bookings
				.stream()
				.filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
				.count();

		BigDecimal totalRevenueOfConfirmedBooking = bookings
				.stream()
				.filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
				.map(Booking::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal avgRevenue = totalConfirmBooking == 0
				? BigDecimal.ZERO
				: totalRevenueOfConfirmedBooking.divide(BigDecimal.valueOf(totalConfirmBooking),
						RoundingMode.HALF_DOWN);
		//
		return new HotelReportDto(totalConfirmBooking, totalRevenueOfConfirmedBooking, avgRevenue);
	}

	@Override
	public List<BookingDto> getMyBookings() {
		User user = getCurrentUser();

		return bookingRepository
				.findByUser(user)
				.stream()
				.map(element -> modelMapper.map(element, BookingDto.class))
				.collect(Collectors.toList());
	}

	public Boolean hasBookingExpired(Booking booking) {
		return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
	}
	// public User getCurrentUser() {
	// User user = (User)
	// SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	// return user;
	// }
	// i added
	// private User getCurrentUser() {
	// Object principal =
	// SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	// if (!(principal instanceof User)) {
	// throw new UnAuthorisedException("User is not authenticated");
	// }
	// return (User) principal;
	// }
}
