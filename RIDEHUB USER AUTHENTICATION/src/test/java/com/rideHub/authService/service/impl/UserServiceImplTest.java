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
import com.rideHub.authService.kafka.publisher.AuthEventPublisher;
import com.rideHub.authService.repository.UserRepository;
import com.rideHub.authService.security.config.JwtProperties;
import com.rideHub.authService.security.jwt.JwtService;
import com.rideHub.authService.security.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImplTest Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private  AuthenticationManager authenticationManager;

    @Mock
    private  JwtService jwtService;

    @Mock
    private  CustomUserDetailsService userDetailsService;

    @Mock
    private  JwtProperties jwtProperties;

    @Mock
    private  AuthEventPublisher authEventPublisher;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    private RegisterRequest registerRequest;
    private User user;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp(){
        registerRequest = RegisterRequest.builder()
                .fullName("Sahasra Muthyala")
                .email("sahasramuthyala04@gmail.com")
                .password("sahasra@04")
                .phone("8688830713")
                .build();

        user = User.builder()
                .id(1L) // DB populated ID
                .fullName("Sahasra Muthyala")
                .email("sahasramuthyala04@gmail.com")
                .password("encodedPassword123") // Mimicking encoded pass
                .phone("8688830713")
                .role(Role.USER)
                .build();

        loginRequest = LoginRequest.builder()
                .email("sahasramuthyala04@gmail.com")
                .password("encodedPassword123")
                .build();
    }

    @Nested
    @DisplayName("Register User Tests")
    class RegisterTest{

        @Test
        @DisplayName("Should create a new user successfully when request is valid and email does not exist")
        void shouldCreateSuccessfully(){

            when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword123");
            when(userRepository.save(any(User.class))).thenReturn(user);
            doNothing().when(authEventPublisher).publishUserRegistered(any(UserRegisteredEvent.class));

            UserResponse response = userServiceImpl.register(registerRequest);

            assertNotNull(response);
            assertEquals(user.getId(), response.getId());
            assertEquals(user.getFullName(), response.getFullName());
            assertEquals(user.getEmail(), response.getEmail());
            assertEquals(user.getPhone(), response.getPhone());
            assertEquals(Role.USER, response.getRole());

            verify(authEventPublisher).publishUserRegistered(any(UserRegisteredEvent.class));
        }

        @Test
        @DisplayName("Should throw EmailAlreadyExistsException when email already exists")
        void shouldThrowEmailAlreadyExistsException(){
            when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

            assertThrows(EmailAlreadyExistsException.class, () -> {
                userServiceImpl.register(registerRequest);
            });
        }
    }


    @Nested
    @DisplayName("User login Tests")
    class UserLoginTests {

        @Test
        @DisplayName("should authenticate user for a valid login request")
        void shouldAuthenticateUser() {

            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(userRepository.findByEmail(loginRequest.getEmail()))
                    .thenReturn(Optional.of(user));
            when(userDetailsService.loadUserByUsername(loginRequest.getEmail()))
                    .thenReturn(userDetails);
            when(jwtService.generateToken(userDetails, user))
                    .thenReturn("fake-jwt-token");
            when(jwtProperties.getExpiration())
                    .thenReturn(3600000L);
            doNothing()
                    .when(authEventPublisher)
                    .publishUserLoggedIn(any(UserLoggedInEvent.class));

            AuthResponse response = userServiceImpl.login(loginRequest);

            assertEquals("fake-jwt-token", response.getToken());
            assertEquals("Bearer", response.getType());
            assertEquals(3600000L, response.getExpiresIn());

            verify(authEventPublisher)
                    .publishUserLoggedIn(any(UserLoggedInEvent.class));
        }

        @Test
        @DisplayName("Authentication fails")
        void shouldThrowAuthenticationException(){
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));


            assertThrows(BadCredentialsException.class,
                    () -> userServiceImpl.login(loginRequest));

            verify(userRepository, never()).findByEmail(anyString());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when authentication successful but user not found")
        void shouldThrowResourceNotFoundException(){

            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> userServiceImpl.login(loginRequest));

            verify(userDetailsService, never()).loadUserByUsername(anyString());
            verify(jwtService, never()).generateToken(any(), any());
        }
    }

    @Nested
    @DisplayName("User profile fetch tests")
    class UserProfileTests{

        @Test
        @DisplayName("should successfully fetch user profile when email exists")
        void shouldFetchUserProfile(){
            String email = "sahasramuthyala04@gmail.com";

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            UserResponse response = userServiceImpl.getProfile(email);
            assertAll(
                    () -> assertEquals(user.getId(), response.getId()),
                    () -> assertEquals(user.getFullName(), response.getFullName()),
                    () -> assertEquals(user.getEmail(), response.getEmail()),
                    () -> assertEquals(user.getPhone(), response.getPhone()),
                    () -> assertEquals(user.getRole(), response.getRole())
            );
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when email does not exist")
        void shouldThrowResourceNotFoundException(){

            String email = "sahasramuthyala04@gmail.com";

            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> userServiceImpl.getProfile(email));
        }
    }
}