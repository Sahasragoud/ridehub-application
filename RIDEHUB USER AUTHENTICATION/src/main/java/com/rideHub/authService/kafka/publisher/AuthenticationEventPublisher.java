package com.rideHub.authService.kafka.publisher;

import com.rideHub.authService.kafka.dto.UserLoggedInEvent;
import com.rideHub.authService.kafka.dto.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class AuthenticationEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserLoggedIn(
            UserLoggedInEvent event
    ){
        kafkaTemplate.send(
                "user-logged-in",
                event.getId().toString(),
                event
        );

        log.info(
                "Published UserLoggedInEvent for user {}",
                event.getId()
        );
    }

    public void publishUserRegistered(
            UserRegisteredEvent event
    ){
        kafkaTemplate.send(
                "user-registered",
                event.getId().toString(),
                event
        );

        log.info(
                "Published UserRegisteredEvent for user{}",
                event.getId()
        );
    }
}
