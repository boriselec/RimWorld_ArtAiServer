package com.boriselec.rimworld.aiart;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

public record Counters(
    Counter rsImageV1,
    Counter rsImageV2,
    Counter rsQueued,
    Counter rsLimit,
    Counter imageSaved) {
    public Counters(MeterRegistry registry) {
        this(
            registry.counter("rs.image.v1"),
            registry.counter("rs.image.v2"),
            registry.counter("rs.queued"),
            registry.counter("rs.limit"),
            registry.counter("image.saved"));
    }
}
