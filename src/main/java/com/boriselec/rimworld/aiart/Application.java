package com.boriselec.rimworld.aiart;

import com.boriselec.rimworld.aiart.generator.GeneratorClient;
import com.boriselec.rimworld.aiart.generator.StaticGeneratorClient;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@SpringBootApplication
@EnableScheduling
public class Application implements SchedulingConfigurer {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public GeneratorClient staticGeneratorClient(
        @Value("${generator.url}") String url,
        @Value("${generator.timeout}") Duration timeout) {
        return new StaticGeneratorClient(url, timeout);
    }

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    public Executor taskExecutor() {
        return Executors.newScheduledThreadPool(10);
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
    }
}
