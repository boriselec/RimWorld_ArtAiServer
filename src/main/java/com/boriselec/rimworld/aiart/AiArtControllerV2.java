package com.boriselec.rimworld.aiart;

import com.boriselec.rimworld.aiart.AiArtControllerV2.HistoryRs.Ready.HistoryRsOutputs.HistoryRsOutputsElem.HistoryRsOutputsImage;
import com.boriselec.rimworld.aiart.data.Request;
import com.boriselec.rimworld.aiart.generator.Model;
import com.boriselec.rimworld.aiart.image.ImageRepository;
import com.boriselec.rimworld.aiart.job.JobQueue;
import com.boriselec.rimworld.aiart.job.QueueLimitException;
import com.boriselec.rimworld.aiart.monitoring.Monitoring;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.boriselec.rimworld.aiart.AiArtControllerV2.HistoryRs.Ready.HistoryRsOutputs;
import static com.boriselec.rimworld.aiart.AiArtControllerV2.HistoryRs.Ready.HistoryRsOutputs.HistoryRsOutputsElem;
import static java.util.Optional.ofNullable;

/**
 * <a href="https://github.com/comfyanonymous/ComfyUI/issues/6607">
 * ComfyUI compatible API
 * </a>
 */
@RestController
@Validated
public class AiArtControllerV2 {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ImageRepository imageRepository;
    private final JobQueue jobQueue;
    private final Monitoring monitoring;

    public AiArtControllerV2(ImageRepository imageRepository, JobQueue jobQueue,
                             Monitoring monitoring) {
        this.imageRepository = imageRepository;
        this.jobQueue = jobQueue;
        this.monitoring = monitoring;
    }

    @PostMapping("/prompt")
    public ResponseEntity<PromptRs> prompt(
        @RequestBody @Valid @NotNull PromptRq rq,
        @RequestParam(required = false) Model model,
        HttpServletRequest httpRequest) {

        log.info("/prompt (%s): %s".formatted(model, rq));

        Request request = Request.deserializeV2(rq, model);
        String userId = ofNullable(httpRequest.getHeader("X-Real-IP"))
            .orElse("unknown");
        try {
            String rqUid = request.uid();

            if (imageRepository.hasImage(rqUid)) {
                return ResponseEntity.ok(new PromptRs(rqUid));
            }

            jobQueue.putIfNotPresent(rqUid, userId, request);
            monitoring.requestQueued();
            return ResponseEntity.ok(new PromptRs(rqUid));
        } catch (QueueLimitException _) {
            monitoring.requestLimited();
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @GetMapping("/history/{rqUid}")
    public ResponseEntity<Map<String, HistoryRs>> history(@PathVariable String rqUid) {
        log.info("/history: " + rqUid);

        Optional<Integer> index = jobQueue.index(rqUid);
        if (imageRepository.hasImage(rqUid)) {
            return ResponseEntity.ok(
                    Map.of(rqUid, new HistoryRs.Ready(outputs(rqUid))));
        } else if (index.isPresent()) {
            return ResponseEntity.ok(
                    Map.of(rqUid, new HistoryRs.Queued(index.get())));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private static HistoryRsOutputs outputs(String rqUid) {
        return new HistoryRsOutputs(
            new HistoryRsOutputsElem(
                List.of(
                    new HistoryRsOutputsImage(
                        rqUid))));
    }

    @GetMapping("/view")
    public ResponseEntity<Resource> view(@RequestParam String filename) {
        log.info("/view: " + filename);
        monitoring.imageQueried(filename);

        return imageRepository.getImage(filename)
            .map(this::getImageResponse)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private ResponseEntity<Resource> getImageResponse(Resource image) {
        monitoring.imageServedV2();
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(image);
    }

    public record PromptRq(
        @NotBlank String prompt,
        @NotBlank String language) {
    }

    public record PromptRs(@JsonProperty("prompt_id") String rqUid) {
    }

    public sealed interface HistoryRs {
        record Queued(int artAiQueuePosition) implements HistoryRs {
        }

        record Ready(HistoryRsOutputs outputs) implements HistoryRs {
            public record HistoryRsOutputs(HistoryRsOutputsElem elem) {
                public record HistoryRsOutputsElem(List<HistoryRsOutputsImage> images) {
                    public record HistoryRsOutputsImage(String filename) {
                    }
                }
            }
        }
    }
}
