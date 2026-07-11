package com.rideHub.authService.service;

import com.rideHub.authService.dto.requests.LoginRequest;
import com.rideHub.authService.dto.requests.RegisterRequest;
import com.rideHub.authService.dto.responses.AuthResponse;
import com.rideHub.authService.dto.responses.UserResponse;

public interface UserService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse getProfile(String email);

}