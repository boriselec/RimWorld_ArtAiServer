package com.boriselec.rimworld.aiart;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

public record Counters(
        Counter gcpRunningSeconds,
        Counter rsImage,
        Counter rsQueued,
        Counter rsLimit) {
    public Counters(MeterRegistry registry) {
        this(
                registry.counter("gcp.running.seconds"),
                registry.counter("rs.image"),
                registry.counter("rs.queued"),
                registry.counter("rs.limit"));
    }
}