package com.boriselec.rimworld.aiart.generator;

import java.io.InputStream;

public interface GeneratorClient {
    InputStream getImage(String description);
}
