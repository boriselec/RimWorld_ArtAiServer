package com.boriselec.rimworld.aiart;

import com.boriselec.rimworld.aiart.data.Request;
import com.boriselec.rimworld.aiart.generator.GcpClient;
import com.boriselec.rimworld.aiart.generator.GeneratorClient;
import com.boriselec.rimworld.aiart.generator.StaticGeneratorClient;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.InstancesSettings;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

@SpringBootApplication
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public LinkedBlockingQueue<Request> linkedBlockingQueue(MeterRegistry meterRegistry) {
        var queue = new LinkedBlockingQueue<Request>(240);
        return meterRegistry.gauge("queue.size", queue, LinkedBlockingQueue::size);
    }

    @Bean
    @ConditionalOnProperty("gcp.project")
    public GcpClient.GcpInstance gcpInstance(@Value("${gcp.project}") String project,
                                             @Value("${gcp.zone}") String zone,
                                             @Value("${gcp.instance}") String instance) {
        return new GcpClient.GcpInstance(project, zone, instance);
    }

    @Bean
    @ConditionalOnProperty("gcp.project")
    public InstancesClient instancesClient(@Value("${gcp.key.path}") String keyPath) throws IOException {
        Credentials credentials = ServiceAccountCredentials.fromStream(new FileInputStream(keyPath));
        CredentialsProvider provider = FixedCredentialsProvider.create(credentials);
        InstancesSettings settings = InstancesSettings.newBuilder()
                .setCredentialsProvider(provider)
                .build();
        return InstancesClient.create(settings);
    }


    @Bean
    @ConditionalOnProperty("generator.url")
    public GeneratorClient staticGeneratorClient(@Value("${generator.url}") String url) {
        return new StaticGeneratorClient(url);
    }

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    public Counters counters(MeterRegistry registry) {
        return new Counters(registry);
    }
}
