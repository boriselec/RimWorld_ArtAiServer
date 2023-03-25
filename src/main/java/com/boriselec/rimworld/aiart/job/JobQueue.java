package com.boriselec.rimworld.aiart.job;

import com.boriselec.rimworld.aiart.Counters;
import com.boriselec.rimworld.aiart.data.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingDeque;

@Component
public class JobQueue {
    private final LinkedBlockingDeque<Request> queue;
    private final Counters counters;
    private final int userLimit;
    private final List<String> priorityUserIds;

    public JobQueue(LinkedBlockingDeque<Request> queue, Counters counters,
                    @Value("${limit.user}") int userLimit,
                    @Value("${priority.userid.list}") List<String> priorityUserIds) {
        this.queue = queue;
        this.counters = counters;
        this.userLimit = userLimit;
        this.priorityUserIds = priorityUserIds;
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
        if (put(request)) {
            counters.queueNew().increment();
            return getExistingPosition(request).orElseThrow();
        } else {
            counters.queueLimitOverall().increment();
            throw new QueueLimitException("Queue is full");
        }
    }

    private boolean put(Request request) {
        return priorityUserIds.contains(request.userId())
                ? queue.offerFirst(request)
                : queue.offer(request);
    }

    private boolean isUserLimitExceeded(String userId) {
        // increase limit for non-steam users, since they have same id
        int refinedUserLimit = "unknown".equals(userId) ? userLimit * 2 : userLimit;
        return queue.stream()
                .map(Request::userId)
                .filter(userId::equals)
                .count() >= refinedUserLimit;
    }
}
