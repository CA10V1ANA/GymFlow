package com.gymflow.pro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GymFlowProApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymFlowProApplication.class, args);
    }
}
