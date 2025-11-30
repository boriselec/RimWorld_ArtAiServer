package com.boriselec.rimworld.aiart.job;

import com.boriselec.rimworld.aiart.data.Request;

import java.util.Optional;
import java.util.function.Consumer;

public interface JobQueue {
    int POSITION_READY = 0;

    int putIfNotPresent(String rqUid, String userId, Request request);

    void processNext(Consumer<Request> consumer);

    boolean isEmpty();

    int size();

    int userSize();

    Optional<Integer> index(String rqUid);

    Optional<Integer> index(String userId, Request request);
}
