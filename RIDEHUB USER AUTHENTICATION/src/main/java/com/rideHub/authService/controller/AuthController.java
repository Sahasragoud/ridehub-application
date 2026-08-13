package com.rideHub.authService.controller;

import com.rideHub.authService.dto.requests.LoginRequest;
import com.rideHub.authService.dto.requests.RegisterRequest;
import com.rideHub.authService.dto.responses.AuthResponse;
import com.rideHub.authService.dto.responses.UserResponse;
import com.rideHub.authService.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        System.out.println(">>> REGISTER CONTROLLER HIT <<<");

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.register(request));
    }

    @Operation(summary = "Authenticate user and generate JWT")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                userService.login(request)
        );
    }

}


