package com.boriselec.rimworld.aiart.monitoring;

import com.boriselec.rimworld.aiart.job.JobQueue;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
class Status {
    private final JobQueue jobQueue;
    private volatile Instant lastSuccessTime;

    Status(JobQueue jobQueue) {
        this.jobQueue = jobQueue;
        this.lastSuccessTime = null;
    }

    int queueSize() {
        return jobQueue.size();
    }

    int queueUserSize() {
        return jobQueue.userSize();
    }

    Instant lastSuccess() {
        return lastSuccessTime;
    }

    void updateLastSuccess() {
        this.lastSuccessTime = Instant.now();
    }
}
