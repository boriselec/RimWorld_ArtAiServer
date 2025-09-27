package com.boriselec.rimworld.aiart.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

@Component
public class ImageFolderLimiter {
    private static final int DELETE_BATCH_SIZE = 100;

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final Path folder;
    private final long folderMaxSize;

    public ImageFolderLimiter(@Value("${image.folder}") String imageFolder,
                              @Value("${image.folder.limit}") String imageFolderLimit) {
        this.folder = Paths.get(imageFolder);
        this.folderMaxSize = DataSize.parse(imageFolderLimit).toBytes();
    }

    public void deleteOld() throws IOException {
        log.info("Deleting old images...");
        while (isExceedSizeLimit()) {
            long deletedCount = deleteOldBatch();
            log.info("Deleted batch: " + deletedCount);
        }
        log.info("Deleting old images done");
    }

    private long deleteOldBatch() throws IOException {
        try (Stream<Path> stream = Files.walk(folder)) {
            return stream
                .map(Path::toFile)
                .filter(File::isFile)
                .sorted(Comparator.comparing(File::lastModified))
                .limit(DELETE_BATCH_SIZE)
                .mapToLong(file -> file.delete() ? 1L : 0L)
                .sum();
        }
    }

    private boolean isExceedSizeLimit() throws IOException {
        try (Stream<Path> stream = Files.walk(folder)) {
            return stream
                .map(Path::toFile)
                .filter(File::isFile)
                .mapToLong(File::length)
                .sum() > folderMaxSize;
        }
    }
}
