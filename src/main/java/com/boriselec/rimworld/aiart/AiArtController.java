package com.boriselec.rimworld.aiart;

import com.boriselec.rimworld.aiart.data.Request;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiArtController {
    private final ImageRepository imageRepository;

    public AiArtController(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestBody String postData) {
        Request rq = Request.deserialize(postData);
        return imageRepository.getImage(rq)
                .map(InputStreamResource::new)
                .map(isr ->
                        ResponseEntity.ok()
                                .contentType(MediaType.IMAGE_PNG)
                                .body(isr))
                .orElseThrow();
    }
}
