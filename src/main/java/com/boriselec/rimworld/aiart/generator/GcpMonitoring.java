package com.boriselec.rimworld.aiart.generator;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
@ConditionalOnProperty("gcp.project")
public class GcpMonitoring {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final GcpClient gcpClient;

    private final AtomicReference<String> status = new AtomicReference<>("");

    public GcpMonitoring(GcpClient gcpClient, MeterRegistry meterRegistry) {
        this.gcpClient = gcpClient;
        meterRegistry.gauge("gcp.status", status, status -> switch (status.get()) {
            default -> 0;
            case "PROVISIONING" -> 1;
            case "STAGING" -> 2;
            case "RUNNING" -> 3;
            case "STOPPING" -> 4;
            case "TERMINATED" -> 5;
        });
    }

    @Scheduled(fixedRate = 1000)
    public void process() {
        String currentStatus = gcpClient.get().getStatus();
        log.info("gcp status: " + currentStatus);
        status.set(currentStatus);
    }
}
