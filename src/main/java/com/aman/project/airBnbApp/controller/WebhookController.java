package com.aman.project.airBnbApp.controller;

import com.aman.project.airBnbApp.service.BookingService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

	private final BookingService bookingService;

	@Value("${stripe.webhook.secret}")
	private String endpointSecret;

	@PostMapping("/payment")
	public ResponseEntity<String> capturePayments(
			@RequestBody String payload,
			@RequestHeader("Stripe-Signature") String sigHeader) {

		if (sigHeader == null || sigHeader.isEmpty()) {
			log.error("Missing Stripe-Signature header");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing Stripe-Signature header");
		}

		try {
			log.info("Verifying Stripe Webhook event signature...");
			Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
			log.info("Event verified successfully: {}", event.getType());

			bookingService.capturePayment(event);

			return ResponseEntity.ok("Success");
		} catch (SignatureVerificationException e) {
			log.error("Stripe signature verification failed: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
		} catch (Exception e) {
			log.error("Error processing Stripe webhook: {}", e.getMessage(), e);
			// We return 200 even on internal processing errors to stop Stripe from retrying
			// if we believe the error is persistent or was already logged.
			return ResponseEntity.ok("Event received but processing failed: " + e.getMessage());
		}
	}
}
