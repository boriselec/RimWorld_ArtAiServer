package com.boriselec.rimworld.aiart;

import com.boriselec.rimworld.aiart.data.Request;
import com.boriselec.rimworld.aiart.generator.GcpGeneratorClient;
import com.boriselec.rimworld.aiart.generator.GeneratorClient;
import com.boriselec.rimworld.aiart.generator.StaticGeneratorClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
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

    @Bean
    @ConditionalOnProperty("generator.url")
    public GeneratorClient staticGeneratorClient(@Value("${generator.url}") String url) {
        return new StaticGeneratorClient(url);
    }

    @Bean
    @ConditionalOnProperty("gcp.project")
    public GeneratorClient gcpGeneratorClient(@Value("${gcp.key.path}") String keyPath,
                                              @Value("${gcp.project}") String project,
                                              @Value("${gcp.zone}") String zone,
                                              @Value("${gcp.instance}") String instance) throws IOException {
        return new GcpGeneratorClient(keyPath, project, zone, instance);
    }
}
