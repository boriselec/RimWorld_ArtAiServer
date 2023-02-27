package com.boriselec.rimworld.aiart;

import com.boriselec.rimworld.aiart.data.Request;
import com.boriselec.rimworld.aiart.job.JobQueue;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@RestController
public class AiArtController {
    private final ImageRepository imageRepository;
    private final JobQueue jobQueue;

    public AiArtController(ImageRepository imageRepository, JobQueue jobQueue) {
        this.imageRepository = imageRepository;
        this.jobQueue = jobQueue;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestBody String postData) {
        Request rq = Request.deserialize(postData);
        return imageRepository.getImage(rq)
                .map(this::getImageResponse)
                .orElseGet(() -> {
                    int position = jobQueue.putIfNotPresent(rq);
                    return getQueuedResponse(position);
                });
    }

    private ResponseEntity<InputStreamResource> getImageResponse(InputStream is) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(new InputStreamResource(is));
    }

    private ResponseEntity<InputStreamResource> getQueuedResponse(int queuePosition) {
        String response = "Queued: " + queuePosition;
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(new InputStreamResource(new ByteArrayInputStream(response.getBytes())));
    }
}
