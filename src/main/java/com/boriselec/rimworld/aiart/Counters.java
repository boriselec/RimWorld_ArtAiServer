package com.boriselec.rimworld.aiart;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public record Counters(
        Counter rsImage,
        Counter rsQueued,
        Counter rsLimit,
        Counter queueNew,
        Counter queuePresent,
        Counter queueLimitOverall,
        Counter queueLimitUser,
        Counter translatedChars) {
    public Counters(MeterRegistry registry) {
        this(
                registry.counter("rs.image"),
                registry.counter("rs.queued"),
                registry.counter("rs.limit"),
                registry.counter("queue.new"),
                registry.counter("queue.present"),
                registry.counter("queue.limit.overall"),
                registry.counter("queue.limit.user"),
                registry.counter("translate.chars." + timestamp()));
    }

    private static String timestamp() {
        return ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT);
    }
}
