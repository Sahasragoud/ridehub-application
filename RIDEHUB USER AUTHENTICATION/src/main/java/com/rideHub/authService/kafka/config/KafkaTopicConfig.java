package com.rideHub.authService.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration

public class KafkaTopicConfig {

    @Bean
    public NewTopic userLoggedInTopic(){
        return new NewTopic("user-logged-in", 1, (short) 1);
    }

    @Bean
    public NewTopic userRegisteredTopic(){
        return new NewTopic("user-registered", 1, (short) 1);
    }
}
