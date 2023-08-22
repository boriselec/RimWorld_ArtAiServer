package com.boriselec.rimworld.aiart.generator;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
@ConditionalOnProperty("gcp.project")
public class GcpMonitoring {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final GcpClient gcpClient;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public GcpMonitoring(GcpClient gcpClient, MeterRegistry meterRegistry) {
        this.gcpClient = gcpClient;
        meterRegistry.gauge("gcp.running", isRunning, isRunning -> isRunning.get() ? 1 : 0);
    }

    @Scheduled(fixedRate = 1000)
    public void process() {
        String status = gcpClient.get().getStatus();
        log.info("gcp status: " + status);
        isRunning.set("RUNNING".equals(status));
    }
}
