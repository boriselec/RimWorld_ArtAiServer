package com.boriselec.rimworld.aiart;

import com.boriselec.rimworld.aiart.image.ImageFolderLimiter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("admin")
public class AdminController {
    private final ImageFolderLimiter imageFolderLimiter;

    public AdminController(ImageFolderLimiter imageFolderLimiter) {
        this.imageFolderLimiter = imageFolderLimiter;
    }

    @PostMapping("/images/limit/ensure")
    public ResponseEntity<?> imagesLimitEnsure() throws IOException {
        imageFolderLimiter.deleteOld();
        return ResponseEntity.ok().build();
    }
}
