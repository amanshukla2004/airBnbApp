package com.aman.project.airBnbApp.controller;

import com.aman.project.airBnbApp.service.BookingService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

	private final BookingService bookingService;

	@Value("${stripe.webhook.secret}")
	private String endpointSecret;

	@PostMapping("/payment")
	public ResponseEntity<Void> capturePayments(
		@RequestBody String payload,
		@RequestHeader("Stripe-Signature") String sigHeader
	) {
		try {
			Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
			bookingService.capturePayment(event);
			return ResponseEntity.noContent().build();
		} catch (SignatureVerificationException e) {
			log.error("Stripe signature verification failed", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		} catch (Exception e) {
			log.error("Internal error processing webhook", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}
