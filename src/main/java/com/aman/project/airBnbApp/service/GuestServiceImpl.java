package com.aman.project.airBnbApp.service;

import com.aman.project.airBnbApp.dto.GuestDto;
import com.aman.project.airBnbApp.entity.Guest;
import com.aman.project.airBnbApp.entity.User;
import com.aman.project.airBnbApp.exception.ResourceNotFoundException;
import com.aman.project.airBnbApp.exception.UnAuthorisedException;
import com.aman.project.airBnbApp.repository.GuestRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aman.project.airBnbApp.repository.BookingRepository;

@Service
@RequiredArgsConstructor
public class GuestServiceImpl implements GuestService {

    private final GuestRepository guestRepository;
    private final BookingRepository bookingRepository;
    private final ModelMapper modelMapper;

    @Override
    public GuestDto addGuest(GuestDto guestDto, User user) {
        Guest guest = modelMapper.map(guestDto, Guest.class);
        guest.setUser(user);
        guest = guestRepository.save(guest);
        return modelMapper.map(guest, GuestDto.class);
    }

    @Override
    public GuestDto updateGuest(Long guestId, GuestDto guestDto, User user) {
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id " + guestId));

        if (!guest.getUser().getId().equals(user.getId())) {
            throw new UnAuthorisedException("You are not authorized to update this guest.");
        }

        guest.setName(guestDto.getName());
        guest.setGender(guestDto.getGender());
        guest.setAge(guestDto.getAge());
        
        guest = guestRepository.save(guest);
        return modelMapper.map(guest, GuestDto.class);
    }

    @Override
    @Transactional
    public void removeGuest(Long guestId, User user) {
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id " + guestId));

        if (!guest.getUser().getId().equals(user.getId())) {
            throw new UnAuthorisedException("You are not authorized to remove this guest.");
        }

        bookingRepository.deleteGuestReferences(guestId);
        guestRepository.delete(guest);
    }

    @Override
    public List<GuestDto> getAllGuestsForUser(User user) {
        // Find all guests belonging to the specific user. 
        // We can just stream or use a repository method.
        // For simplicity, let's assume the repository has a findByUser method.
        List<Guest> guests = guestRepository.findByUser(user);
        return guests.stream()
                .map(guest -> modelMapper.map(guest, GuestDto.class))
                .collect(Collectors.toList());
    }
}
