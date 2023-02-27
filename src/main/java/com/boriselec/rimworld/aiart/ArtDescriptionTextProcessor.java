package com.boriselec.rimworld.aiart;

import com.boriselec.rimworld.aiart.data.Request;

public class ArtDescriptionTextProcessor {
    public static String getDescription(Request request) {
        return request.thingDesc() + " " + request.artDesc();
    }
}
