package com.boriselec.rimworld.aiart.generator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

public interface GeneratorClient {
    InputStream getImage(String description)
        throws IOException, InterruptedException, URISyntaxException;
}
