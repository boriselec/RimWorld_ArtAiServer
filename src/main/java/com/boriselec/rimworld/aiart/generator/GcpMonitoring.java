package com.boriselec.rimworld.aiart.generator;

import com.boriselec.rimworld.aiart.Counters;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty("gcp.project")
public class GcpMonitoring {
    private final GcpClient gcpClient;
    private final Counters counters;

    public GcpMonitoring(GcpClient gcpClient, Counters counters) {
        this.gcpClient = gcpClient;
        this.counters = counters;
    }

    @Scheduled(fixedRate = 1000)
    public void process() {
        boolean isRunning = "RUNNING".equals(gcpClient.get().getStatus());
        if (isRunning) {
            counters.gcpRunningSeconds().increment();
        }
    }
}
