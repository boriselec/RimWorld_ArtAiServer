package com.boriselec.rimworld.aiart.generator;

import java.io.IOException;
import java.io.InputStream;

public interface GeneratorClient {
    InputStream getImage(String prompt)
        throws IOException, InterruptedException;
}
