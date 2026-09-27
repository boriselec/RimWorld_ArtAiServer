package com.boriselec.rimworld.aiart.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Monitor how long it takes for player to view generated image.
 */
@Component
public class ImageRequestMonitoring {
    private static final Duration TTL = Duration.ofDays(14);

    private final Map<String, Instant> filenamesToGenTime;
    private final Timer queried;
    private final Counter expired;

    public ImageRequestMonitoring(MeterRegistry meterRegistry) {
        this.filenamesToGenTime = new ConcurrentHashMap<>();
        this.queried = meterRegistry.timer("image.queried");
        this.expired = meterRegistry.counter("image.not_queried.expired");
        meterRegistry.gauge("image.not_queried", filenamesToGenTime, Map::size);
    }

    /**
     * Put in-progress filename for monitoring.
     * In progress means that image is ready, but not queried yet.
     */
    public void putGenerated(String filename) {
        filenamesToGenTime.put(filename, Instant.now());
    }

    /**
     * Finish request, player queried image.
     */
    public void finish(String filename) {
        Instant putOnDate = filenamesToGenTime.remove(filename);
        if (putOnDate != null) {
            queried.record(Duration.between(putOnDate, Instant.now()));
        }
    }

    /**
     * Give up on images no player ever queried.
     */
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    public void evictNotQueried() {
        Instant deadline = Instant.now().minus(TTL);
        filenamesToGenTime.values().removeIf(putOnDate -> {
            if (putOnDate.isBefore(deadline)) {
                expired.increment();
                return true;
            } else {
                return false;
            }
        });
    }
}
