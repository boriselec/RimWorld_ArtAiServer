package com.boriselec.rimworld.aiart.job;

import com.boriselec.rimworld.aiart.ArtDescriptionTextProcessor;
import com.boriselec.rimworld.aiart.image.ImageRepository;
import com.boriselec.rimworld.aiart.data.ArtDescription;
import com.boriselec.rimworld.aiart.data.Request;
import com.boriselec.rimworld.aiart.generator.GeneratorClient;
import com.boriselec.rimworld.aiart.generator.GeneratorNotReadyException;
import com.boriselec.rimworld.aiart.translate.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.concurrent.LinkedBlockingDeque;

@Component
public class JobProcessor {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final LinkedBlockingDeque<Request> queue;
    private final ImageRepository imageRepository;
    private final GeneratorClient generatorClient;
    private final Translator translator;

    public JobProcessor(LinkedBlockingDeque<Request> queue,
                        ImageRepository imageRepository,
                        GeneratorClient generatorClient,
                        Translator translator) {
        this.queue = queue;
        this.imageRepository = imageRepository;
        this.generatorClient = generatorClient;
        this.translator = translator;
    }

    @Scheduled(fixedDelay = 1000)
    public void process() throws Exception {
        while (!queue.isEmpty()) {
            Request request = queue.peek();
            String englishDescription = prepare(request);
            log.info("Prepared description (%s): %s".formatted(request.language(), englishDescription));
            try (InputStream image = generatorClient.getImage(englishDescription)) {
                String filePath = imageRepository.getFilePath(request.getArtDescription());
                imageRepository.saveImage(image, filePath, englishDescription);
                //noinspection ResultOfMethodCallIgnored
                queue.remove(request);
                log.info("Queue size: " + queue.size());
            } catch (GeneratorNotReadyException e) {
                log.warn(e.getMessage());
                return;
            }
        }
    }

    private String prepare(Request request) {
        ArtDescription originalDesc = request.getArtDescription();
        ArtDescription preparedDesc = ArtDescriptionTextProcessor.process(originalDesc);
        ArtDescription englishDesc = translator.translateFrom(request.language(), preparedDesc);
        return englishDesc.toString();
    }
}
