package com.boriselec.rimworld.aiart;

import com.boriselec.rimworld.aiart.data.Request;

public class ArtDescriptionTextProcessor {
    private static final String TALE_DATE_REGEX = " on [^ ]+ of (Aprimay|Jugust|Septober|Decembary)[^.]+.";
    private static final String DRESSER_UNRELATED = "Placing more than one dresser near the same bed has no effect\\. ";
    // some modes adds info in <i> tags
    private static final String ITALIC_TAG = "<i>.*</i>";
    // (Vanilla Furniture Expanded - Art)
    private static final String MOD_FURNITURE = "\\(Vanilla Furniture Expanded - Art\\)";

    public static String getDescription(Request request) {
        String description = request.thingDesc() + " " + request.artDesc();
        return description.replaceAll(TALE_DATE_REGEX, ".")
                .replaceAll(DRESSER_UNRELATED, "")
                .replaceAll(ITALIC_TAG, "")
                .replaceAll(MOD_FURNITURE, "")
                .replaceAll("\\n", "")
                .replaceAll("  +", " ");
    }
}
