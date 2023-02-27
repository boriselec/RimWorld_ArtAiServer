package com.boriselec.rimworld.aiart;

import com.boriselec.rimworld.aiart.data.Request;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.LinkedBlockingQueue;

@SpringBootApplication
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public LinkedBlockingQueue<Request> linkedBlockingQueue() {
        return new LinkedBlockingQueue<>(240);
    }
}
