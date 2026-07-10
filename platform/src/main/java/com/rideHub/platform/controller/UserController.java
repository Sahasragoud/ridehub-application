package com.rideHub.platform.controller;

import com.rideHub.platform.dto.responses.UserResponse;
import com.rideHub.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
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
