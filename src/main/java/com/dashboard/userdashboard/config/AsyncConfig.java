package com.dashboard.userdashboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
    // No code needed.
    // @EnableAsync  — activates @Async on service methods
    // @EnableScheduling — activates @Scheduled for cleanup jobs
    // The annotations do all the work.
}
