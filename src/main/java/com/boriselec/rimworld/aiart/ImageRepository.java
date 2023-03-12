package com.boriselec.rimworld.aiart;

import com.boriselec.rimworld.aiart.data.ArtDescription;
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

@Component
public class ImageRepository {
    private final String imageFolder;

    public ImageRepository(@Value("${image.folder}") String imageFolder) {
        this.imageFolder = imageFolder;
    }

    public Optional<InputStream> getImage(ArtDescription desc) {
        try {
            return Optional.of(
                    new DataInputStream(
                            new FileInputStream(getFilePath(desc))));
        } catch (FileNotFoundException e) {
            return Optional.empty();
        }
    }

    public void saveImage(InputStream is, String filePath, String descriptionMetadata) throws IOException {
        try (ImageInputStream stream = ImageIO.createImageInputStream(is)) {
            ImageReader reader = null;
            ImageWriter writer = null;
            try {
                reader = ImageIO.getImageReaders(stream).next();
                reader.setInput(stream, true, true);
                IIOImage image = reader.readAll(0, null);

                IIOMetadataNode root = new IIOMetadataNode(IIOMetadataFormatImpl.standardMetadataFormatName);
                appendMetadata(root, "Software", "boriselec.com");
                appendMetadata(root, "Description", descriptionMetadata);
                image.getMetadata().mergeTree(IIOMetadataFormatImpl.standardMetadataFormatName, root);

                writer = ImageIO.getImageWriter(reader);
                try (ImageOutputStream output = ImageIO.createImageOutputStream(new File(filePath))) {
                    writer.setOutput(output);
                    writer.write(image);
                }
            } finally {
                Optional.ofNullable(reader).ifPresent(ImageReader::dispose);
                Optional.ofNullable(writer).ifPresent(ImageWriter::dispose);
            }
        }
    }

    public String getFilePath(ArtDescription desc) {
        return imageFolder + desc.hashCode() + ".png";
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
