package com.aman.project.airBnbApp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/public/config")
@RequiredArgsConstructor
public class PublicConfigController {

    @Value("${support.email:support@noxplatform.com}")
    private String supportEmail;

    @GetMapping
    public ResponseEntity<Map<String, String>> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("supportEmail", supportEmail);
        return ResponseEntity.ok(config);
    }
}
