package com.boriselec.rimworld.aiart.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Monitor how long it takes for player to view generated image.
 */
@Component
public class ImageRequestMonitoring {
    private final Map<String, LocalDateTime> filenamesToGenTime;
    private final MeterRegistry meterRegistry;

    public ImageRequestMonitoring(MeterRegistry meterRegistry) {
        this.filenamesToGenTime = new HashMap<>();
        this.meterRegistry = meterRegistry;
        meterRegistry.gauge("image.not_queried", filenamesToGenTime, Map::size);
    }

    /**
     * Put in-progress filename for monitoring.
     * In progress means that image is ready, but not queried yet.
     */
    public void putGenerated(String filename) {
        filenamesToGenTime.put(filename, LocalDateTime.now());
    }

    /**
     * Finish request, player queried image.
     */
    public void finish(String filename) {
        filenamesToGenTime.computeIfPresent(filename, (_, putOnDate) -> {
            Duration duration = Duration.between(putOnDate, LocalDateTime.now());
            meterRegistry.summary("image.queried.seconds").record(duration.toSeconds());
            return null;
        });
    }
}