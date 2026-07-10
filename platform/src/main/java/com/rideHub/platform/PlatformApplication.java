package com.rideHub.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PlatformApplication {

	public static Logger log = LoggerFactory.getLogger(PlatformApplication.class);
	public static void main(String[] args) {
		log.info("RideHub Application has been started");
		SpringApplication.run(PlatformApplication.class, args);
		log.info("RideHub Application has been ended");

	}

}
