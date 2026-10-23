package com.boriselec.rimworld.aiart.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Single entry point for metrics and status.
 */
@Component
public class Monitoring {
    private final Counters counters;
    private final Status status;
    private final ImageRequestMonitoring imageRequestMonitoring;

    Monitoring(MeterRegistry registry, Status status,
               ImageRequestMonitoring imageRequestMonitoring) {
        this.counters = new Counters(registry);
        this.status = status;
        this.imageRequestMonitoring = imageRequestMonitoring;
    }

    public void requestQueued() {
        counters.rsQueued().increment();
    }

    public void requestLimited() {
        counters.rsLimit().increment();
    }

    public void imageSaved(String filename) {
        counters.imageSaved().increment();
        status.updateLastSuccess();
        imageRequestMonitoring.putGenerated(filename);
    }

    public void imageRejected() {
        counters.imageRejected().increment();
    }

    public void imageQueried(String filename) {
        imageRequestMonitoring.finish(filename);
    }

    public void imageServedV1() {
        counters.rsImageV1().increment();
    }

    public void imageServedV2() {
        counters.rsImageV2().increment();
    }
}
