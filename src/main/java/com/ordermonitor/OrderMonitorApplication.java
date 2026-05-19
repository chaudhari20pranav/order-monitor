package com.ordermonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Order Monitor platform.
 *
 * @EnableAsync    – allows EmailService methods to run in background threads
 * @EnableScheduling – enables the admin reminder scheduler
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class OrderMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderMonitorApplication.class, args);
    }
}
