package com.rideHub.authService.kafka.dto;

import com.rideHub.authService.enums.Role;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {

    private Long id;

    private String fullName;

    private String email;

    private String phone;

    private Role role;

    private LocalDateTime occurredAt;
}
