package com.boriselec.rimworld.aiart.generator;

/**
 * Generator answered 4xx
 */
public class GeneratorRejectedException extends RuntimeException {
    public GeneratorRejectedException(String message) {
        super(message);
    }
}
