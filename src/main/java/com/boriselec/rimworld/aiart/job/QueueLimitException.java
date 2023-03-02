package com.boriselec.rimworld.aiart.job;

public class QueueLimitException extends RuntimeException {
    public QueueLimitException(String message) {
        super(message);
    }
}
