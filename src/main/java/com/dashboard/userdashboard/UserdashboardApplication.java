package com.dashboard.userdashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UserdashboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserdashboardApplication.class, args);
	}

}
