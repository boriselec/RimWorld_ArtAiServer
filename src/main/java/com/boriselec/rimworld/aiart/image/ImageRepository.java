package com.boriselec.rimworld.aiart.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

@Component
public class ImageRepository {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final String imageFolder;

    public ImageRepository(@Value("${image.folder}") String imageFolder) {
        this.imageFolder = imageFolder;
    }

    public String getPromptUid(String prompt) {
        return UUID.nameUUIDFromBytes(prompt.getBytes()).toString();
    }

    public Optional<InputStream> getImage(String filename) {
        try {
            return Optional.of(
                new DataInputStream(
                    new FileInputStream(getFilePath(filename))));
        } catch (FileNotFoundException _) {
            return Optional.empty();
        }
    }

    public boolean hasImage(String filename) {
        return new File(getFilePath(filename))
            .exists();
    }

    public void saveImage(InputStream is, String filename, String processedPrompt)
        throws IOException {

        try (ImageInputStream stream = ImageIO.createImageInputStream(is)) {
            ImageReader reader = null;
            ImageWriter writer = null;
            try {
                reader = ImageIO.getImageReaders(stream).next();
                reader.setInput(stream, true, true);
                IIOImage image = reader.readAll(0, null);

                IIOMetadataNode root = new IIOMetadataNode(
                    IIOMetadataFormatImpl.standardMetadataFormatName);
                appendMetadata(root, "Software", "boriselec.com");
                appendMetadata(root, "Description", processedPrompt);
                image.getMetadata().mergeTree(
                    IIOMetadataFormatImpl.standardMetadataFormatName, root);

                writer = ImageIO.getImageWriter(reader);
                String filePath = getFilePath(filename);
                File file = new File(filePath);
                try (ImageOutputStream output = ImageIO.createImageOutputStream(file)) {
                    writer.setOutput(output);
                    writer.write(image);
                }
                log.info("New image: %s: %s".formatted(processedPrompt, filePath));
            } finally {
                Optional.ofNullable(reader).ifPresent(ImageReader::dispose);
                Optional.ofNullable(writer).ifPresent(ImageWriter::dispose);
            }
        }
    }

    private String getFilePath(String filename) {
        return imageFolder + filename + ".png";
    }

    private void appendMetadata(IIOMetadataNode root, String key, String value) {
        IIOMetadataNode textEntry = new IIOMetadataNode("TextEntry");
        textEntry.setAttribute("keyword", key);
        textEntry.setAttribute("value", value);

        IIOMetadataNode text = new IIOMetadataNode("Text");
        text.appendChild(textEntry);

        root.appendChild(text);
    }
}