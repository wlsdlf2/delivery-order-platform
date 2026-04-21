package com.sparta.deliveryorderplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DeliveryOrderPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeliveryOrderPlatformApplication.class, args);
    }

}
