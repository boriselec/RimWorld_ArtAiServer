package com.boriselec.rimworld.aiart;

import com.boriselec.rimworld.aiart.data.Request;
import com.boriselec.rimworld.aiart.data.RequestWithUserId;
import com.boriselec.rimworld.aiart.image.ImageRepository;
import com.boriselec.rimworld.aiart.job.JobQueue;
import com.boriselec.rimworld.aiart.job.QueueLimitException;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Deprecated
@RestController
public class AiArtController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ImageRepository imageRepository;
    private final JobQueue jobQueue;
    private final Counters counters;

    public AiArtController(ImageRepository imageRepository, JobQueue jobQueue,
                           Counters counters) {
        this.imageRepository = imageRepository;
        this.jobQueue = jobQueue;
        this.counters = counters;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generate(
        @RequestBody String postData, HttpServletRequest httpRequest) {

        log.info("Received /generate: " + postData);
        log.info("IP: " + httpRequest.getHeader("X-Real-IP"));

        var rq = Request.deserialize(postData);
        String filename = imageRepository.getPromptUid(rq.value().prompt());
        return imageRepository.getImage(filename)
            .map(this::getImageResponse)
            .orElseGet(() -> process(rq));
    }

    private ResponseEntity<InputStreamResource> process(RequestWithUserId rq) {
        String response;
        try {
            int position = jobQueue.putIfNotPresent("", rq.userId(), rq.value());
            response = "Image is generating... Please wait" +
                "\n\nQueued: " + position;
            counters.rsQueued().increment();
        } catch (QueueLimitException e) {
            log.info(e.getMessage());
            response = e.getMessage() + ". Try later.";
            counters.rsLimit().increment();
        }
        return getInProgressResponse(response);
    }

    private ResponseEntity<InputStreamResource> getImageResponse(InputStream is) {
        counters.rsImageV1().increment();
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(new InputStreamResource(is));
    }

    private ResponseEntity<InputStreamResource> getInProgressResponse(String response) {
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .body(new InputStreamResource(
                new ByteArrayInputStream(response.getBytes())));
    }
}
