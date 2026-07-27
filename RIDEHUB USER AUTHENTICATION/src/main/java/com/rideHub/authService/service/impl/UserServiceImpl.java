package com.rideHub.authService.service.impl;

import com.rideHub.authService.dto.requests.LoginRequest;
import com.rideHub.authService.dto.requests.RegisterRequest;
import com.rideHub.authService.dto.responses.AuthResponse;
import com.rideHub.authService.dto.responses.UserResponse;
import com.rideHub.authService.entity.User;
import com.rideHub.authService.enums.Role;
import com.rideHub.authService.exception.EmailAlreadyExistsException;
import com.rideHub.authService.exception.ResourceNotFoundException;
import com.rideHub.authService.kafka.dto.UserLoggedInEvent;
import com.rideHub.authService.kafka.dto.UserRegisteredEvent;
import com.rideHub.authService.kafka.publisher.AuthenticationEventPublisher;
import com.rideHub.authService.repository.UserRepository;
import com.rideHub.authService.security.config.JwtProperties;
import com.rideHub.authService.security.jwt.JwtService;
import com.rideHub.authService.security.service.CustomUserDetailsService;
import com.rideHub.authService.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private static final Logger log =
            LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final JwtProperties jwtProperties;
    private final AuthenticationEventPublisher authenticationEventPublisher;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {

        log.info("Register request received for email: {}", request.getEmail());

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

        authenticationEventPublisher.publishUserRegistered(
                UserRegisteredEvent.builder()
                        .id(savedUser.getId())
                        .fullName(savedUser.getFullName())
                        .email(savedUser.getEmail())
                        .phone(savedUser.getPhone())
                        .role(savedUser.getRole())
                        .build()
        );

        log.info("User registered successfully with ID: {}", savedUser.getId());

        return mapToUserResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        log.info("Login attempt for email: {}", request.getEmail());

        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        }
        catch(AuthenticationException ex){
            log.warn("Failed login attempt for email: {}", request.getEmail());
            throw ex;
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found."));

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(request.getEmail());

        String token = jwtService.generateToken(userDetails, user);

        authenticationEventPublisher.publishUserLoggedIn(
                UserLoggedInEvent.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .role(user.getRole())
                        .build()
        );

        log.info("User '{}' authenticated successfully.", request.getEmail());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .expiresIn(jwtProperties.getExpiration())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
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