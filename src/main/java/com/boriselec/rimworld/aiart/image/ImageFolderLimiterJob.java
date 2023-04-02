package com.boriselec.rimworld.aiart.image;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ImageFolderLimiterJob {
    private final ImageFolderLimiter imageFolderLimiter;

    public ImageFolderLimiterJob(ImageFolderLimiter imageFolderLimiter) {
        this.imageFolderLimiter = imageFolderLimiter;
    }

    @Scheduled(cron = "${image.folder.limit.cron}")
    public void process() throws IOException {
        imageFolderLimiter.deleteOld();
    }
}
