package com.boriselec.rimworld.aiart.generator;

/**
 * Generator rejected the prompt itself, permanent
 */
public class GeneratorRejectedException extends RuntimeException {
    public GeneratorRejectedException(String message) {
        super(message);
    }
}
