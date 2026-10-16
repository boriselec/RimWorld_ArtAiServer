package com.boriselec.rimworld.aiart.generator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

public interface GeneratorClient {
    /**
     * @param seed null: the generator derives it from the prompt
     */
    InputStream getImage(String prompt, Integer seed)
        throws IOException, InterruptedException, URISyntaxException;
}
