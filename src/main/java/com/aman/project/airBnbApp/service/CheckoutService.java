package com.aman.project.airBnbApp.service;

import com.aman.project.airBnbApp.entity.Booking;

public interface CheckoutService {
	String getCheckoutSession(Booking booking, String successUrl, String failureUrl);
}
