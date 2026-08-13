package com.rideHub.authService.security.service;

import com.rideHub.authService.entity.User;
import com.rideHub.authService.enums.Role;
import com.rideHub.authService.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerDetailSerivce Tests")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private String email;
    private User user;

    @BeforeEach
    void setUp(){
        email = "sahasramuthyala04@gmail.com";

        user = User.builder()
                .id(1L)
                .fullName("Sahasra")
                .phone("8688830713")
                .password("enchoded123")
                .email(email)
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("Should load user successfully when email exists")
    void shouldLoadUserDetailsForExistingUser(){

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDetails userdetails = userDetailsService.loadUserByUsername(email);

        assertAll(
                () -> assertEquals(user.getEmail(), userdetails.getUsername()),
                () -> assertEquals(user.getPassword(), userdetails.getPassword()),
                () -> assertTrue(userdetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
        );

        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user not exists")
    void shouldThrowUsernameNotFoundExceptionForUnknownEmail(){
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(email));
    }
}