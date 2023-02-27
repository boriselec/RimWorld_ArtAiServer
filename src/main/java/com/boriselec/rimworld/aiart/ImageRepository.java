package com.boriselec.rimworld.aiart;

import com.boriselec.rimworld.aiart.data.Request;
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

    public Optional<InputStream> getImage(Request rq) {
        try {
            return Optional.of(
                    new DataInputStream(
                            new FileInputStream(getFilePath(rq))));
        } catch (FileNotFoundException e) {
            return Optional.empty();
        }
    }

    public void saveImage(InputStream is, Request rq) throws IOException {
        BufferedImage images = ImageIO.read(is);
        ImageIO.write(images, "png", new File(getFilePath(rq)));
    }

    private String getFilePath(Request rq) {
        return IMAGE_FOLDER + rq.hashCode() + ".png";
    }
}
