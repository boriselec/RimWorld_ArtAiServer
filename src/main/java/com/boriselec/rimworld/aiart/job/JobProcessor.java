package com.boriselec.rimworld.aiart.job;

import com.boriselec.rimworld.aiart.ArtDescriptionTextProcessor;
import com.boriselec.rimworld.aiart.monitoring.Monitoring;
import com.boriselec.rimworld.aiart.data.Request;
import com.boriselec.rimworld.aiart.generator.GeneratorClient;
import com.boriselec.rimworld.aiart.generator.GeneratorRejectedException;
import com.boriselec.rimworld.aiart.generator.Model;
import com.boriselec.rimworld.aiart.image.ImageRepository;
import com.boriselec.rimworld.aiart.translate.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class JobProcessor {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final JobQueue queue;
    private final ImageRepository imageRepository;
    private final GeneratorClient generatorClient;
    private final Translator translator;
    private final Monitoring monitoring;

    public JobProcessor(JobQueue queue, ImageRepository imageRepository,
                        GeneratorClient generatorClient, Translator translator,
                        Monitoring monitoring) {
        this.queue = queue;
        this.imageRepository = imageRepository;
        this.generatorClient = generatorClient;
        this.translator = translator;
        this.monitoring = monitoring;
    }

    @Scheduled(fixedDelay = 1000)
    public void process() {
        while (!queue.isEmpty()) {
            queue.processNext(request -> {
                try {
                    generate(request);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void generate(Request request) throws IOException, InterruptedException {
        String processedPrompt = prepare(request);
        Model model = request.model();
        log.info("Prepared prompt (%s): %s".formatted(
            request.language(), processedPrompt));
        String filename = request.uid();

        try (InputStream image = generatorClient.getImage(model, processedPrompt)) {
            imageRepository.saveImage(image, filename, processedPrompt);
            monitoring.imageSaved(filename);
        } catch (GeneratorRejectedException e) {
            log.error("Generator rejected prompt: " + e.getMessage());
            imageRepository.saveRejectedImage(filename, processedPrompt);
            monitoring.imageRejected();
        }
    }

    private String prepare(Request request) {
        String originalDesc = request.prompt();
        String preparedDesc = ArtDescriptionTextProcessor.process(originalDesc);
        return translator.translateFrom(request.language(), preparedDesc);
    }
}
