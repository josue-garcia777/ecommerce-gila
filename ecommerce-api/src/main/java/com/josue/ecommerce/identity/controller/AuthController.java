package com.josue.ecommerce.identity.controller;

import com.josue.ecommerce.identity.dto.AccessTokenResponse;
import com.josue.ecommerce.identity.dto.LoginRequest;
import com.josue.ecommerce.identity.dto.RegisterRequest;
import com.josue.ecommerce.identity.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    ResponseEntity<AccessTokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity
                .created(URI.create("/api/v1/auth/login"))
                .body(authenticationService.register(request));
    }

    @PostMapping("/login")
    AccessTokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticationService.login(request);
    }
}
