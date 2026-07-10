package com.sahil.hospital_appointment.controller;

import com.sahil.hospital_appointment.dto.ApiResponse;
import com.sahil.hospital_appointment.dto.request.LoginRequest;
import com.sahil.hospital_appointment.dto.request.RegisterRequest;
import com.sahil.hospital_appointment.dto.response.AuthResponse;
import com.sahil.hospital_appointment.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public-facing auth endpoints. Both routes are whitelisted in
 * SecurityConfig (Step 6) via permitAll() on /api/auth/** - no token
 * required to hit either one.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Login successful", response));
    }
}