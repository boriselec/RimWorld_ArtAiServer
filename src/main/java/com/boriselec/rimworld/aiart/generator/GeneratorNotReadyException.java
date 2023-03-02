package com.boriselec.rimworld.aiart.generator;

import java.io.IOException;

public class GeneratorNotReadyException extends IOException {
    public GeneratorNotReadyException(String message) {
        super(message);
    }
}
