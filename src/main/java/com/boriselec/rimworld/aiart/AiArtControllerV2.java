package com.boriselec.rimworld.aiart;

import com.boriselec.rimworld.aiart.AiArtControllerV2.HistoryRs.HistoryRsOutputs.HistoryRsOutputsElem.HistoryRsOutputsImage;
import com.boriselec.rimworld.aiart.data.Request;
import com.boriselec.rimworld.aiart.data.RequestWithUserId;
import com.boriselec.rimworld.aiart.image.ImageRepository;
import com.boriselec.rimworld.aiart.job.JobQueue;
import com.boriselec.rimworld.aiart.job.QueueLimitException;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.List;

import static com.boriselec.rimworld.aiart.AiArtControllerV2.HistoryRs.HistoryRsOutputs;
import static com.boriselec.rimworld.aiart.AiArtControllerV2.HistoryRs.HistoryRsOutputs.HistoryRsOutputsElem;

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
    private final Counters counters;

    public AiArtControllerV2(ImageRepository imageRepository, JobQueue jobQueue,
                             Counters counters) {
        this.imageRepository = imageRepository;
        this.jobQueue = jobQueue;
        this.counters = counters;
    }

    @PostMapping("/prompt")
    public ResponseEntity<PromptRs> prompt(
        @RequestBody @Valid PromptRq rq) {

        log.info("/prompt: " + rq.toString());
        RequestWithUserId request = Request.deserializeV2(rq);

        try {
            String rqUid = imageRepository.getPromptUid(request.value().prompt());
            int pos = jobQueue.putIfNotPresent(
                rqUid, 
                request.userId(),
                request.value());
            counters.rsQueued().increment();
            return ResponseEntity.ok(new PromptRs(rqUid, pos));
        } catch (QueueLimitException e) {
            counters.rsLimit().increment();
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @GetMapping("/history/{rqUid}")
    public @ResponseBody HistoryRs history(@PathVariable String rqUid) {
        log.info("/history: " + rqUid);

        if (imageRepository.hasImage(rqUid)) {
            return new HistoryRs(
                null,
                new HistoryRsOutputs(
                    new HistoryRsOutputsElem(
                        List.of(
                            new HistoryRsOutputsImage(
                                rqUid)))));
        } else {
            Integer index = jobQueue.index(rqUid)
                .orElse(null);
            return new HistoryRs(index, null);
        }
    }

    @GetMapping("/view")
    public ResponseEntity<InputStreamResource> view(@RequestParam String filename) {
        log.info("/view: " + filename);

        return imageRepository.getImage(filename)
            .map(this::getImageResponse)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private ResponseEntity<InputStreamResource> getImageResponse(InputStream is) {
        counters.rsImage().increment();
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(new InputStreamResource(is));
    }

    public record PromptRq(@NotNull @Valid PromptRqData artAi) {
        public record PromptRqData(
            @NotBlank String prompt,
            @NotBlank String userId,
            @NotBlank String language) {
        }
    }

    public record PromptRs(@JsonProperty("prompt_id") String rqUid,
                           int artAiQueuePosition) {
    }

    public record HistoryRs(Integer artAiQueuePosition, HistoryRsOutputs outputs) {
        public record HistoryRsOutputs(HistoryRsOutputsElem elem) {
            public record HistoryRsOutputsElem(List<HistoryRsOutputsImage> images) {
                public record HistoryRsOutputsImage(String filename) {
                }
            }
        }
    }
}
