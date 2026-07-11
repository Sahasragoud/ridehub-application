package com.rideHub.authService.controller;

import com.rideHub.authService.dto.responses.UserResponse;
import com.rideHub.authService.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> profile(
            Authentication authentication) {

        return ResponseEntity.ok(
                userService.getProfile(authentication.getName())
        );
    }

}
