package com.boriselec.rimworld.aiart.job;

import com.boriselec.rimworld.aiart.ArtDescriptionTextProcessor;
import com.boriselec.rimworld.aiart.Counters;
import com.boriselec.rimworld.aiart.data.ArtDescription;
import com.boriselec.rimworld.aiart.data.Request;
import com.boriselec.rimworld.aiart.generator.GeneratorClient;
import com.boriselec.rimworld.aiart.image.ImageRepository;
import com.boriselec.rimworld.aiart.translate.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

@Component
public class JobProcessor {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final JobQueue queue;
    private final ImageRepository imageRepository;
    private final GeneratorClient generatorClient;
    private final Translator translator;
    private final Counters counters;

    public JobProcessor(JobQueue queue,
                        ImageRepository imageRepository,
                        GeneratorClient generatorClient,
                        Translator translator,
                        Counters counters) {
        this.queue = queue;
        this.imageRepository = imageRepository;
        this.generatorClient = generatorClient;
        this.translator = translator;
        this.counters = counters;
    }

    @Scheduled(fixedDelay = 1000)
    public void process() {
        while (!queue.isEmpty()) {
            queue.processNext(request -> {
                try {
                    String englishDescription = prepare(request);
                    log.info("Prepared description (%s): %s".formatted(request.language(), englishDescription));

                    InputStream image = generatorClient.getImage(englishDescription);
                    String filename = request.getArtDescription().uid();
                    imageRepository.saveImage(image, filename, englishDescription);
                    counters.imageSaved().increment();
                } catch (IOException | InterruptedException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private String prepare(Request request) {
        ArtDescription originalDesc = request.getArtDescription();
        ArtDescription preparedDesc = ArtDescriptionTextProcessor.process(originalDesc);
        ArtDescription englishDesc = translator.translateFrom(request.language(), preparedDesc);
        return englishDesc.toString();
    }
}
