package com.rideHub.platform.service.impl;

import com.rideHub.platform.dto.requests.LoginRequest;
import com.rideHub.platform.dto.requests.RegisterRequest;
import com.rideHub.platform.dto.responses.AuthResponse;
import com.rideHub.platform.dto.responses.UserResponse;
import com.rideHub.platform.entity.User;
import com.rideHub.platform.enums.Role;
import com.rideHub.platform.exception.EmailAlreadyExistsException;
import com.rideHub.platform.exception.InvalidCredentialsException;
import com.rideHub.platform.exception.ResourceNotFoundException;
import com.rideHub.platform.repository.UserRepository;
import com.rideHub.platform.security.jwt.JwtService;
import com.rideHub.platform.security.service.CustomUserDetailsService;
import com.rideHub.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.rideHub.platform.PlatformApplication.log;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public UserResponse register(RegisterRequest request) {

        log.info("Registering user {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists.");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);

        log.info("User registered successfully {}", savedUser.getEmail());

        return mapToUserResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(request.getEmail());

        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .expiresIn(86400000L)
                .build();
    }

    @Override
    public UserResponse getProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found."));

        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .build();
    }
}