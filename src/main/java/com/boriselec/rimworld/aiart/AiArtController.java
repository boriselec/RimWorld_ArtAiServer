package com.boriselec.rimworld.aiart;

import com.boriselec.rimworld.aiart.data.Request;
import com.boriselec.rimworld.aiart.image.ImageRepository;
import com.boriselec.rimworld.aiart.job.JobQueue;
import com.boriselec.rimworld.aiart.job.QueueLimitException;
import com.boriselec.rimworld.aiart.monitoring.Monitoring;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Deprecated
@RestController
public class AiArtController {
    private static final String SUNSET_NOTICE =
        "Mod update required.\n\n" +
        "This version of AI Art uses an old server API\n" +
        "that will be switched off at the end of 2026.";

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ImageRepository imageRepository;
    private final JobQueue jobQueue;
    private final Monitoring monitoring;

    public AiArtController(ImageRepository imageRepository, JobQueue jobQueue,
                           Monitoring monitoring) {
        this.imageRepository = imageRepository;
        this.jobQueue = jobQueue;
        this.monitoring = monitoring;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generate(
        @RequestBody String postData, HttpServletRequest httpRequest) {

        log.info("Received /generate: " + postData);

        var rq = Request.deserialize(postData);
        var userId = Optional.ofNullable(httpRequest.getHeader("X-Real-IP"))
            .orElse("unknown");
        String filename = rq.uid();
        return imageRepository.getImage(filename)
            .map(image -> {
                monitoring.imageQueried(filename);
                return getImageResponse(image);
            })
            .orElseGet(() -> process(rq, userId));
    }

    private ResponseEntity<Resource> process(Request rq, String userId) {
        try {
            jobQueue.putIfNotPresent("", userId, rq);
            monitoring.requestQueued();
        } catch (QueueLimitException e) {
            log.info(e.getMessage());
            monitoring.requestLimited();
            return getInProgressResponse(e.getMessage() + ". Try later.");
        }
        return getInProgressResponse(SUNSET_NOTICE);
    }

    private ResponseEntity<Resource> getImageResponse(Resource image) {
        monitoring.imageServedV1();
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(image);
    }

    private ResponseEntity<Resource> getInProgressResponse(String response) {
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .body(new ByteArrayResource(response.getBytes()));
    }
}
