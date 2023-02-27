package com.boriselec.rimworld.aiart.job;

import com.boriselec.rimworld.aiart.ArtDescriptionTextProcessor;
import com.boriselec.rimworld.aiart.ImageRepository;
import com.boriselec.rimworld.aiart.data.Request;
import com.boriselec.rimworld.aiart.generator.GeneratorClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class JobProcessor {
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

    @Scheduled(fixedDelay = 100)
    public void process() throws IOException {
        if (!queue.isEmpty()) {
            Request request = queue.poll();
            String description = ArtDescriptionTextProcessor.getDescription(request);
            InputStream image = generatorClient.getImage(description);
            imageRepository.saveImage(image, request);
        }
    }
}
