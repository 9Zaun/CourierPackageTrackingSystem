package com.couriertracker.config;

import com.couriertracker.core.CourierService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CourierTrackerConfiguration {

    @Bean
    public CourierService courierService() {
        return new CourierService("CourierTracker");
    }
}