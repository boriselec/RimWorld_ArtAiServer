package com.boriselec.rimworld.aiart.generator;

import java.io.IOException;
import java.io.InputStream;

public interface GeneratorClient {
    InputStream getImage(Model model, String prompt)
        throws IOException, InterruptedException;
}
