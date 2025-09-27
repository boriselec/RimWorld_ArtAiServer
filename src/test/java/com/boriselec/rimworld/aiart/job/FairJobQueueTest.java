package com.boriselec.rimworld.aiart.job;

import com.boriselec.rimworld.aiart.data.Request;
import com.boriselec.rimworld.aiart.translate.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FairJobQueueTest {
    private JobQueue jobQueue;

    private Request request1;
    private Request request2;
    private Request request3;

    @BeforeEach
    void setUp() {
        jobQueue = new FairJobQueue(3, 2); // maxUsers=3, maxRequestsByUser=2
        request1 = new Request("req1", Language.ENGLISH);
        request2 = new Request("req2", Language.ENGLISH);
        request3 = new Request("req3", Language.ENGLISH);
    }

    @Test
    void testPutIfNotPresent_SingleUser() {
        int position = jobQueue.putIfNotPresent("user1", request1);

        assertEquals(0, position);
        assertEquals(1, jobQueue.size());
        assertFalse(jobQueue.isEmpty());
    }

    @Test
    void testPutIfNotPresent_MultipleRequests() {
        jobQueue.putIfNotPresent("user1", request1);
        int position = jobQueue.putIfNotPresent("user1", request2);

        assertEquals(1, position);
        assertEquals(2, jobQueue.size());
    }

    @Test
    void testPutIfNotPresent_ExceedsRequestLimit() {
        jobQueue.putIfNotPresent("user1", request1);
        jobQueue.putIfNotPresent("user1", request2);

        assertThrows(QueueLimitException.class, () -> {
            jobQueue.putIfNotPresent("user1", request3);
        });
    }

    @Test
    void testPutIfNotPresent_ExceedsUserLimit() {
        jobQueue.putIfNotPresent("user1", request1);
        jobQueue.putIfNotPresent("user2", request2);
        jobQueue.putIfNotPresent("user3", request3);

        Request request4 = new Request("req4", Language.ENGLISH);
        assertThrows(QueueLimitException.class, () -> {
            jobQueue.putIfNotPresent("user4", request4);
        });
    }

    @Test
    void testProcess_Next_SingleRequest() {
        jobQueue.putIfNotPresent("user1", request1);

        AtomicInteger processedCount = new AtomicInteger(0);
        List<Request> processedRequests = new ArrayList<>();

        jobQueue.processNext(request -> {
            processedRequests.add(request);
            processedCount.incrementAndGet();
        });

        assertEquals(1, processedCount.get());
        assertEquals(1, processedRequests.size());
        assertEquals(request1, processedRequests.get(0));
        assertEquals(0, jobQueue.size());
        assertTrue(jobQueue.isEmpty());
    }

    @Test
    void testProcess_Next_RoundRobin() {
        jobQueue.putIfNotPresent("user1", request1);
        jobQueue.putIfNotPresent("user2", request2);

        List<Request> processedRequests = new ArrayList<>();

        jobQueue.processNext(processedRequests::add);
        jobQueue.processNext(processedRequests::add);

        assertEquals(2, processedRequests.size());
        assertTrue(processedRequests.contains(request1));
        assertTrue(processedRequests.contains(request2));
        assertTrue(jobQueue.isEmpty());
    }

    @Test
    void testProcess_Next_MultipleRequestsPerUser() {
        jobQueue.putIfNotPresent("user1", request1);
        jobQueue.putIfNotPresent("user1", request2);
        jobQueue.putIfNotPresent("user2", request3);

        List<Request> processedRequests = new ArrayList<>();

        // Process all requests
        jobQueue.processNext(processedRequests::add); // Should process user1's first request
        jobQueue.processNext(processedRequests::add); // Should process user2's request
        jobQueue.processNext(processedRequests::add); // Should process user1's second request

        assertEquals(3, processedRequests.size());
        assertEquals(request1, processedRequests.get(0));
        assertEquals(request3, processedRequests.get(1));
        assertEquals(request2, processedRequests.get(2));
        assertTrue(jobQueue.isEmpty());
    }

    @Test
    void testProcess_Next_EmptyQueue() {
        AtomicInteger processedCount = new AtomicInteger(0);
        jobQueue.processNext(request -> processedCount.incrementAndGet());

        assertEquals(0, processedCount.get());
    }

    @Test
    void testIndex_FirstRequest() {
        int position = jobQueue.putIfNotPresent("user1", request1);

        assertEquals(0, position);
        assertEquals(0, jobQueue.index("user1", request1));
    }

    @Test
    void testIndex_MultipleUsers() {
        jobQueue.putIfNotPresent("user1", request1);
        jobQueue.putIfNotPresent("user2", request2);
        jobQueue.putIfNotPresent("user1", request3);

        assertEquals(0, jobQueue.index("user1", request1));
        assertEquals(1, jobQueue.index("user2", request2));
        assertEquals(2, jobQueue.index("user1", request3));
    }

    @Test
    void testIndex_RequestNotFound() {
        jobQueue.putIfNotPresent("user1", request1);

        assertEquals(-1, jobQueue.index("user1", request2));
        assertEquals(-1, jobQueue.index("user2", request1));
    }

    @Test
    void testIndex_EmptyQueue() {
        assertEquals(-1, jobQueue.index("user1", request1));
    }

    @Test
    void testSize_EmptyQueue() {
        assertEquals(0, jobQueue.size());
    }

    @Test
    void testSize_MultipleRequests() {
        jobQueue.putIfNotPresent("user1", request1);
        jobQueue.putIfNotPresent("user2", request2);
        jobQueue.putIfNotPresent("user1", request3);

        assertEquals(3, jobQueue.size());
    }

    @Test
    void testIsEmpty() {
        assertTrue(jobQueue.isEmpty());

        jobQueue.putIfNotPresent("user1", request1);
        assertFalse(jobQueue.isEmpty());

        jobQueue.processNext(request -> {
        });
        assertTrue(jobQueue.isEmpty());
    }

    @Test
    void testComplexRoundRobinScenario() {
        // User1: 2 requests, User2: 1 request, User3: 2 requests
        Request req1_1 = new Request("user1_req1", Language.ENGLISH);
        Request req1_2 = new Request("user1_req2", Language.ENGLISH);
        Request req2_1 = new Request("user2_req1", Language.ENGLISH);
        Request req3_1 = new Request("user3_req1", Language.ENGLISH);
        Request req3_2 = new Request("user3_req2", Language.ENGLISH);

        jobQueue.putIfNotPresent("user1", req1_1);
        jobQueue.putIfNotPresent("user2", req2_1);
        jobQueue.putIfNotPresent("user3", req3_1);
        jobQueue.putIfNotPresent("user1", req1_2);
        jobQueue.putIfNotPresent("user3", req3_2);

        List<Request> processedRequests = new ArrayList<>();

        // Process all requests and verify round-robin order
        while (!jobQueue.isEmpty()) {
            jobQueue.processNext(processedRequests::add);
        }

        assertEquals(5, processedRequests.size());

        // Expected order: user1_req1, user2_req1, user3_req1, user1_req2, user3_req2
        assertEquals(req1_1, processedRequests.get(0));
        assertEquals(req2_1, processedRequests.get(1));
        assertEquals(req3_1, processedRequests.get(2));
        assertEquals(req1_2, processedRequests.get(3));
        assertEquals(req3_2, processedRequests.get(4));
    }

    @Test
    void testProcessNextWithException() {
        jobQueue.putIfNotPresent("user1", request1);
        jobQueue.putIfNotPresent("user1", request2);

        assertThrows(RuntimeException.class, () -> {
            jobQueue.processNext(request -> {
                throw new RuntimeException("Processing failed");
            });
        });

        // No changes
        assertEquals(2, jobQueue.size());
    }

    @Test
    void testIndexCalculationAfterProcessing() {
        jobQueue.putIfNotPresent("user1", request1);
        jobQueue.putIfNotPresent("user2", request2);
        jobQueue.putIfNotPresent("user1", request3);

        // Initial indices
        assertEquals(0, jobQueue.index("user1", request1));
        assertEquals(1, jobQueue.index("user2", request2));
        assertEquals(2, jobQueue.index("user1", request3));

        // Process first request (user1's first)
        jobQueue.processNext(request -> {
        });

        // Indices should update
        assertEquals(0, jobQueue.index("user2", request2));
        assertEquals(1, jobQueue.index("user1", request3));
        assertEquals(-1, jobQueue.index("user1", request1)); // No longer in queue
    }

    @Test
    void testSame() {
        jobQueue.putIfNotPresent("user1", new Request("user1", Language.ENGLISH));
        int pos = jobQueue.putIfNotPresent("user1", new Request("user1", Language.ENGLISH));

        assertEquals(1, jobQueue.size());
        assertEquals(0, pos);
    }
}