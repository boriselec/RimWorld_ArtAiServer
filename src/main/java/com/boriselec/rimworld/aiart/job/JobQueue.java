package com.boriselec.rimworld.aiart.job;

import com.boriselec.rimworld.aiart.Counters;
import com.boriselec.rimworld.aiart.data.Request;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class JobQueue {
    private final LinkedBlockingQueue<Request> queue;
    private final Counters counters;

    public JobQueue(LinkedBlockingQueue<Request> queue, Counters counters) {
        this.queue = queue;
        this.counters = counters;
    }

    /**
     * @return position in queue (started with 0)
     */
    public int putIfNotPresent(Request request) {
        return getExistingPosition(request)
                .orElseGet(() -> putInQueue(request));
    }

    private Optional<Integer> getExistingPosition(Request request) {
        Object[] array = queue.toArray();
        for (int i = 0; i < array.length; i++) {
            Request queued = (Request) array[i];
            if (queued.getArtDescription().equals(request.getArtDescription())) {
                counters.queuePresent().increment();
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    private int putInQueue(Request request) {
        if (isUserLimitExceeded(request.userId())) {
            counters.queueLimitUser().increment();
            throw new QueueLimitException("Limit exceeded");
        }
        if (queue.offer(request)) {
            counters.queueNew().increment();
            return queue.size() - 1;
        } else {
            counters.queueLimitOverall().increment();
            throw new QueueLimitException("Queue is full");
        }
    }

    private boolean isUserLimitExceeded(String userId) {
        return queue.stream()
                .map(Request::userId)
                .filter(userId::equals)
                .count() > 10;
    }
}
