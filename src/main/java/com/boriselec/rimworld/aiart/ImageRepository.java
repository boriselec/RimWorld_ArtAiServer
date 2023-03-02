package com.boriselec.rimworld.aiart;

import com.boriselec.rimworld.aiart.data.ArtDescription;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Component
public class ImageRepository {
    private static final String IMAGE_FOLDER = "/mnt/images/";

    public Optional<InputStream> getImage(ArtDescription desc) {
        try {
            return Optional.of(
                    new DataInputStream(
                            new FileInputStream(getFilePath(desc))));
        } catch (FileNotFoundException e) {
            return Optional.empty();
        }
    }

    public void saveImage(InputStream is, ArtDescription desc) throws IOException {
        BufferedImage images = ImageIO.read(is);
        ImageIO.write(images, "png", new File(getFilePath(desc)));
    }

    private String getFilePath(ArtDescription desc) {
        return IMAGE_FOLDER + desc.hashCode() + ".png";
    }
}
