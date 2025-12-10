package com.boriselec.rimworld.aiart.job;

import com.boriselec.rimworld.aiart.data.Request;
import com.boriselec.rimworld.aiart.data.RequestWithUserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@Component
public class FairJobQueue implements JobQueue {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final int maxUsers;
    private final int maxRequestsByUser;

    private final Map<String, Queue<Request>> userQueues;
    private final LinkedList<String> userRing;
    private final ReentrantLock lock;
    private final Map<String, RequestWithUserId> rqByUid;

    public FairJobQueue(@Value("${limit.users}") int maxUsers,
                        @Value("${limit.requestsByUser}") int maxRequestsByUser) {
        this.maxUsers = maxUsers;
        this.maxRequestsByUser = maxRequestsByUser;

        userQueues = new ConcurrentHashMap<>();
        userRing = new LinkedList<>();
        lock = new ReentrantLock();
        rqByUid = new ConcurrentHashMap<>();
    }

    /**
     * @return position in queue (started with 0)
     */
    @Override
    public int putIfNotPresent(String rqUid, String userId, Request request) {
        lock.lock();
        try {
            Queue<Request> userQueue = userQueues.computeIfAbsent(
                userId, k -> new LinkedList<>());

            // Check if request already exists
            if (userQueue.contains(request)) {
                return index(userId, request)
                    .orElseThrow();
            }

            // Check user limit
            if (userQueue.size() >= maxRequestsByUser) {
                throw new QueueLimitException("Request limit exceeded");
            }

            userQueue.offer(request);
            rqByUid.put(rqUid, new RequestWithUserId(userId, request));

            // Add user to ring if they weren't in it
            if (!userRing.contains(userId)) {
                if (userRing.size() >= maxUsers) {
                    throw new QueueLimitException("User limit exceeded");
                }
                userRing.offer(userId);
            }
        } finally {
            log.info(toString());
            lock.unlock();
        }
        return index(userId, request)
            .orElseThrow();
    }

    /**
     * Get request from queue in round-robin fashion, process it,
     * if no error - delete from queue
     */
    @Override
    public void processNext(Consumer<Request> consumer) {
        String currentUserId;
        Request request;

        // First, peek at the next request without removing it
        lock.lock();
        try {
            if (userRing.isEmpty()) {
                return; // No users with pending requests
            }

            // Peek at next user in round-robin fashion
            currentUserId = userRing.peekFirst();
            Queue<Request> userQueue = userQueues.get(currentUserId);

            if (userQueue == null || userQueue.isEmpty()) {
                // Clean up: remove user from ring if queue is empty
                userRing.pollFirst();
                return;
            }

            // Peek at the request without removing it
            request = userQueue.peek();
        } finally {
            lock.unlock();
        }

        // Process outside of lock
        if (request != null) {
            consumer.accept(request);

            // Only if processing succeeded, remove the request and update ring
            lock.lock();
            try {
                Queue<Request> userQueue = userQueues.get(currentUserId);
                if (userQueue != null && !userQueue.isEmpty()) {
                    // Verify this is still the same request
                    // (in case of concurrent modifications)
                    Request headRequest = userQueue.peek();
                    if (request.equals(headRequest)) {
                        userQueue.poll(); // Remove the processed request

                        // Update ring position
                        userRing.pollFirst(); // Remove user from front
                        if (!userQueue.isEmpty()) {
                            // Add back to end if more requests
                            userRing.offerLast(currentUserId);
                        }
                    } else {
                        log.error("Unexpected state: request changed");
                    }
                }
            } finally {
                log.info(toString());
                lock.unlock();
            }
        } else {
            log.error("Unexpected state: request is null");
        }

        if (isEmpty()) {
            rqByUid.clear();
        }
    }

    @Override
    public boolean isEmpty() {
        return userRing.isEmpty();
    }

    /**
     * Returns the total number of requests in the queue across all users
     *
     * @return total number of pending requests
     */
    @Override
    public int size() {
        lock.lock();
        try {
            return userQueues.values().stream()
                .mapToInt(Queue::size)
                .sum();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the total number of requests in the queue across all users
     *
     * @return total number of pending requests
     */
    @Override
    public int userSize() {
        lock.lock();
        try {
            return userRing.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<Integer> index(String rqUid) {
        return ofNullable(rqByUid.get(rqUid))
            .flatMap(rq -> index(rq.userId(), rq.value()));
    }

    /**
     * Returns how many processing steps until the given request will be processed
     *
     * @param userId  the user who owns the request
     * @param request the request to find
     * @return number of processing steps before this request, or -1 if not found
     */
    @Override
    public Optional<Integer> index(String userId, Request request) {
        lock.lock();
        try {
            if (userRing.isEmpty()) {
                return empty();
            }

            // First, check if the request exists in the user's queue
            Queue<Request> targetUserQueue = userQueues.get(userId);
            if (targetUserQueue == null || targetUserQueue.isEmpty()) {
                return empty();
            }

            // Find position of request within user's queue
            int positionInUserQueue = -1;
            int pos = 0;
            for (Request req : targetUserQueue) {
                if (request.equals(req)) {
                    positionInUserQueue = pos;
                    break;
                }
                pos++;
            }

            if (positionInUserQueue == -1) {
                return empty(); // Request not found in user's queue
            }

            // Find user's position in the ring
            int userPositionInRing = -1;
            int ringPos = 0;
            for (String ringUserId : userRing) {
                if (ringUserId.equals(userId)) {
                    userPositionInRing = ringPos;
                    break;
                }
                ringPos++;
            }

            if (userPositionInRing == -1) {
                // User not in ring (shouldn't happen if queue is not empty)
                return empty();
            }

            int steps = 0;

            // Count complete rounds before reaching our user
            int completeRounds = positionInUserQueue;
            for (int round = 0; round < completeRounds; round++) {
                for (String ringUserId : userRing) {
                    Queue<Request> userQueue = userQueues.get(ringUserId);
                    if (userQueue != null && userQueue.size() > round) {
                        steps++;
                    }
                }
            }

            // Count steps in the final round up to our user
            int userIndex = 0;
            for (String ringUserId : userRing) {
                if (userIndex < userPositionInRing) {
                    Queue<Request> userQueue = userQueues.get(ringUserId);
                    if (userQueue != null && userQueue.size() > positionInUserQueue) {
                        steps++;
                    }
                } else {
                    break; // Reached our user
                }
                userIndex++;
            }

            return Optional.of(steps);

        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        return "JobQueue{" +
            "size=" + size() +
            ", userRingSize=" + userSize() +
            '}';
    }
}
