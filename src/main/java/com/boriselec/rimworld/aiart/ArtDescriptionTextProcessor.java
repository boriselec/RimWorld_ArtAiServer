package com.boriselec.rimworld.aiart;

import com.boriselec.rimworld.aiart.data.ArtDescription;

public class ArtDescriptionTextProcessor {
    private static final String TALE_DATE_REGEX = " on [^ ]+ of (Aprimay|Jugust|Septober|Decembary)[^.]+.";
    private static final String TALE_DATE_REGEX_RUSSIAN = " [^ ]+ (Мартомай|Июгуст|Сентоноябрь|Декавраль)[^.]+.";
    private static final String DRESSER_UNRELATED = "Placing more than one dresser near the same bed has no effect\\.";
    private static final String CRIB_UNRELATED = "A high quality crib will make a baby happy\\. Multiple cribs can be placed in a bedroom without turning it into a barracks\\.";
    // some modes adds info in <i> tags
    private static final String ITALIC_TAG = "<i>.*</i>";
    private static final String COLOUR_TAG = "<.?color[^>]*>";
    private static final String TALENT = "Talent:";
    // Vanilla Expanded
    private static final String VANILLA_EXPANDED = "\\(Vanilla [^)]*Expanded[^)]*\\)";

    public static ArtDescription process(ArtDescription description) {
        return new ArtDescription(
            process(description.artDesc()),
            process(description.thingDesc()));
    }

    private static String process(String description) {
        return description.replaceAll(TALE_DATE_REGEX, ".")
            .replaceAll(TALE_DATE_REGEX_RUSSIAN, ".")
            .replaceAll(DRESSER_UNRELATED, "")
            .replaceAll(CRIB_UNRELATED, "")
            .replaceAll(ITALIC_TAG, "")
            .replaceAll(COLOUR_TAG, "")
            .replaceAll(TALENT, "")
            .replaceAll(VANILLA_EXPANDED, "")
            .replaceAll("\\n", "")
            .replaceAll("  +", " ")
            .trim();
    }
}
