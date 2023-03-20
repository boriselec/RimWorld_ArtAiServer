package com.boriselec.rimworld.aiart;

import com.boriselec.rimworld.aiart.data.Request;

public class ArtDescriptionTextProcessor {
    private static final String TALE_DATE_REGEX = " on [^ ]+ of (Aprimay|Jugust|Septober|Decembary)[^.]+.";
    private static final String DRESSER_UNRELATED = "Placing more than one dresser near the same bed has no effect\\. ";
    private static final String CRIB_UNRELATED = "A high quality crib will make a baby happy\\. Multiple cribs can be placed in a bedroom without turning it into a barracks\\. ";
    // some modes adds info in <i> tags
    private static final String ITALIC_TAG = "<i>.*</i>";
    // Vanilla Expanded
    private static final String VANILLA_EXPANDED = "\\(Vanilla [^)]*Expanded[^)]*\\)";

    public static String getDescription(Request request) {
        String description = request.thingDesc() + " " + request.artDesc();
        return description.replaceAll(TALE_DATE_REGEX, ".")
                .replaceAll(DRESSER_UNRELATED, "")
                .replaceAll(CRIB_UNRELATED, "")
                .replaceAll(ITALIC_TAG, "")
                .replaceAll(VANILLA_EXPANDED, "")
                .replaceAll("\\n", "")
                .replaceAll("  +", " ");
    }
}
