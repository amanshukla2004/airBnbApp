package com.aman.project.airBnbApp.service;

import com.aman.project.airBnbApp.entity.Booking;
import com.aman.project.airBnbApp.entity.User;
import com.aman.project.airBnbApp.repository.BookingRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutServiceImpl implements CheckoutService {

	private final BookingRepository bookingRepository;

	@Override
	public String getCheckoutSession(Booking booking, String successUrl, String failureUrl) {
		log.info("Creating session for booking with ID :  {}", booking.getId());
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		try {
			CustomerCreateParams customerParam = CustomerCreateParams
				.builder()
				.setName(user.getName())
				.setEmail(user.getEmail())
				.build();
			Customer customer = Customer.create(customerParam);
			SessionCreateParams sessionParams = SessionCreateParams
				.builder()
				.setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)
				.setMode(SessionCreateParams.Mode.PAYMENT)
				.setCustomer(customer.getId())
				.setSuccessUrl(successUrl)
				.setCancelUrl(failureUrl)
				.putMetadata("bookingId", booking.getId().toString())
				.setPaymentIntentData(
					SessionCreateParams.PaymentIntentData
						.builder()
						.putMetadata("bookingId", booking.getId().toString())
						.build()
				)
				.addLineItem(
					SessionCreateParams.LineItem
						.builder()
						.setQuantity(1L)
						.setPriceData(
							SessionCreateParams.LineItem.PriceData
								.builder()
								.setCurrency("inr")
								.setUnitAmount(booking.getAmount().multiply(BigDecimal.valueOf(100)).longValue())
								.setProductData(
									SessionCreateParams.LineItem.PriceData.ProductData
										.builder()
										.setName(booking.getHotel().getName() + " : " + booking.getRoom().getType())
										.setDescription("Booking ID:" + booking.getId())
										.build()
								)
								.build()
						)
						.build()
				)
				.build();

			Session session = Session.create(sessionParams);

			booking.setPaymentSessionId(session.getId());
			bookingRepository.save(booking);

			log.info("Session created successfully for booking with ID :  {}", booking.getId());

			return session.getUrl();
		} catch (StripeException e) {
			throw new RuntimeException(e);
		}
	}
}
