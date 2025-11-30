package com.boriselec.rimworld.aiart;

import com.boriselec.rimworld.aiart.job.JobQueue;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class Status {
    private final JobQueue jobQueue;
    private volatile Instant lastSuccessTime;

    public Status(JobQueue jobQueue) {
        this.jobQueue = jobQueue;
        this.lastSuccessTime = null;
    }

    public int queueSize() {
        return jobQueue.size();
    }

    public Instant lastSuccess() {
        return lastSuccessTime;
    }

    public void updateLastSuccess() {
        this.lastSuccessTime = Instant.now();
    }
}
