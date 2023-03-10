package com.boriselec.rimworld.aiart.data;

import com.boriselec.rimworld.aiart.translate.Language;

public record Request(String artDesc, String thingDesc, String userId, Language language) {
    public static Request deserialize(String data) {
        String[] split = data.split(";");

        String artDesc = split[0];
        String thingDesc = split[1];
        String userId = split[2];
        Language language = split.length > 3
                ? Language.fromRimworldCode(split[3])
                : Language.ENGLISH;

        return new Request(artDesc, thingDesc, userId, language);
    }

    public ArtDescription getArtDescription() {
        return new ArtDescription(artDesc, thingDesc);
    }
}
