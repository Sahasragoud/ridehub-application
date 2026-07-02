package com.rideHub.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PlatformApplication {

	public static Logger LOGGER = LoggerFactory.getLogger(PlatformApplication.class);
	public static void main(String[] args) {
		LOGGER.info("RideHub Application has been started");
		SpringApplication.run(PlatformApplication.class, args);
		LOGGER.info("RideHub Application has been ended");

	}

}
