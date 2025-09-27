package com.boriselec.rimworld.aiart.job;

import com.boriselec.rimworld.aiart.data.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@Component
public class FairJobQueue implements JobQueue {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final int maxUsers;
    private final int maxRequestsByUser;

    private final Map<String, Queue<Request>> userQueues;
    private final LinkedList<String> userRing;
    private final ReentrantLock lock;

    public FairJobQueue(@Value("${limit.users}") int maxUsers,
                        @Value("${limit.requestsByUser}") int maxRequestsByUser) {
        this.maxUsers = maxUsers;
        this.maxRequestsByUser = maxRequestsByUser;

        userQueues = new ConcurrentHashMap<>();
        userRing = new LinkedList<>();
        lock = new ReentrantLock();
    }

    /**
     * @return position in queue (started with 0)
     */
    @Override
    public int putIfNotPresent(String userId, Request request) {
        lock.lock();
        try {
            Queue<Request> userQueue = userQueues.computeIfAbsent(
                userId, k -> new LinkedList<>());

            // Check if request already exists
            if (userQueue.contains(request)) {
                return index(userId, request);
            }

            // Check user limit
            if (userQueue.size() >= maxRequestsByUser) {
                throw new QueueLimitException("Request limit exceeded");
            }

            boolean wasEmpty = userQueue.isEmpty();
            userQueue.offer(request);

            // Add user to ring if they weren't in it (had empty queue)
            if (wasEmpty && !userRing.contains(userId)) {
                if (userRing.size() >= maxUsers) {
                    throw new QueueLimitException("User limit exceeded");
                }
                userRing.offer(userId);
            }
        } finally {
            log.info(toString());
            lock.unlock();
        }
        return index(userId, request);
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
     * Returns how many processing steps until the given request will be processed
     *
     * @param userId  the user who owns the request
     * @param request the request to find
     * @return number of processing steps before this request, or -1 if not found
     */
    @Override
    public int index(String userId, Request request) {
        lock.lock();
        try {
            if (userRing.isEmpty()) {
                return -1;
            }

            // First, check if the request exists in the user's queue
            Queue<Request> targetUserQueue = userQueues.get(userId);
            if (targetUserQueue == null || targetUserQueue.isEmpty()) {
                return -1;
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
                return -1; // Request not found in user's queue
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
                return -1; // User not in ring (shouldn't happen if queue is not empty)
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

            return steps;

        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        return "JobQueue{" +
            "size=" + size() +
            ", userRingSize=" + userRing.size() +
            '}';
    }
}