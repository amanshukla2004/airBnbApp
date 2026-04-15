package com.aman.project.airBnbApp.service;

import com.aman.project.airBnbApp.dto.GuestDto;
import com.aman.project.airBnbApp.entity.User;
import java.util.List;

public interface GuestService {
    GuestDto addGuest(GuestDto guestDto, User user);
    GuestDto updateGuest(Long guestId, GuestDto guestDto, User user);
    void removeGuest(Long guestId, User user);
    List<GuestDto> getAllGuestsForUser(User user);
}
