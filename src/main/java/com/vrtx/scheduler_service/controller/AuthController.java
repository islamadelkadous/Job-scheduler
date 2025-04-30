package com.vrtx.scheduler_service.controller;

import com.vrtx.scheduler_service.model.dto.AuthRequest;
import com.vrtx.scheduler_service.model.dto.AuthResponse;
import com.vrtx.scheduler_service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(value = "/authenticate")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody @Valid AuthRequest authRequest) {
        return ResponseEntity.ok(authService.authenticateUser(authRequest));
    }
}
