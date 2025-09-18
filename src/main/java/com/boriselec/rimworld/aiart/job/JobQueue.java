package com.boriselec.rimworld.aiart.job;

import com.boriselec.rimworld.aiart.data.Request;

import java.util.function.Consumer;

public interface JobQueue {
    int putIfNotPresent(String userId, Request request);

    void processNext(Consumer<Request> consumer);

    boolean isEmpty();

    int size();

    int index(String userId, Request request);
}
