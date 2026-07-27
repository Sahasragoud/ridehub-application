package com.rideHub.authService.kafka.dto;

import com.rideHub.authService.enums.Role;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoggedInEvent {

    private Long id;

    private String fullName;

    private String email;

    private String phone;

    private Role role;
}
