package com.boriselec.rimworld.aiart;

import com.boriselec.rimworld.aiart.data.Request;
import org.springframework.stereotype.Component;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;

@Component
public class ImageRepository {
    private static final String IMAGE_FOLDER = "/mnt/images/";

    public Optional<InputStream> getImage(Request rq) {
        try {
            return Optional.of(
                    new DataInputStream(
                            new FileInputStream(IMAGE_FOLDER + rq.hashCode() + "")));
        } catch (FileNotFoundException e) {
            return Optional.empty();
        }
    }
}
