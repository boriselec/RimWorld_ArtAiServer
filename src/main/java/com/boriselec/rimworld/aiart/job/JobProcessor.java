package com.boriselec.rimworld.aiart.job;

import com.boriselec.rimworld.aiart.ArtDescriptionTextProcessor;
import com.boriselec.rimworld.aiart.ImageRepository;
import com.boriselec.rimworld.aiart.data.Request;
import com.boriselec.rimworld.aiart.generator.GeneratorNotReadyException;
import com.boriselec.rimworld.aiart.generator.GeneratorClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class JobProcessor {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final LinkedBlockingQueue<Request> queue;
    private final ImageRepository imageRepository;
    private final GeneratorClient generatorClient;

    public JobProcessor(LinkedBlockingQueue<Request> queue,
                        ImageRepository imageRepository,
                        GeneratorClient generatorClient) {
        this.queue = queue;
        this.imageRepository = imageRepository;
        this.generatorClient = generatorClient;
    }

    @Scheduled(fixedDelay = 1000)
    public void process() throws Exception {
        if (!queue.isEmpty()) {
            Request request = queue.peek();
            String description = ArtDescriptionTextProcessor.getDescription(request);
            try {
                InputStream image = generatorClient.getImage(description);
                imageRepository.saveImage(image, request.getArtDescription());
                queue.remove();
                log.info("Queue size: " + queue.size());
            } catch (GeneratorNotReadyException e) {
                log.warn(e.getMessage());
            }
        }
    }
}
