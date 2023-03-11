package com.boriselec.rimworld.aiart;

import com.boriselec.rimworld.aiart.data.Request;

public class ArtDescriptionTextProcessor {
    private static final String TALE_DATE_REGEX = " on [^ ]+ of (Aprimay|Jugust|Septober|Decembary)[^.]+.";

    public static String getDescription(Request request) {
        String description = request.thingDesc() + " " + request.artDesc();
        return description.replaceAll(TALE_DATE_REGEX, ".");
    }
}
