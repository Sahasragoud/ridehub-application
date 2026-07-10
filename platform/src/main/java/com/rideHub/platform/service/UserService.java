package com.rideHub.platform.service;

import com.rideHub.platform.dto.requests.LoginRequest;
import com.rideHub.platform.dto.requests.RegisterRequest;
import com.rideHub.platform.dto.responses.AuthResponse;
import com.rideHub.platform.dto.responses.UserResponse;

public interface UserService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse getProfile(String email);

}