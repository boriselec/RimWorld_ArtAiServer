package com.boriselec.rimworld.aiart.data;

public record Request(String artDesc, String thingDesc, String userId) {
    public static Request deserialize(String data) {
        String[] split = data.split(";");
        return new Request(split[0], split[1], split[2]);
    }

    public ArtDescription getArtDescription() {
        return new ArtDescription(artDesc, thingDesc);
    }
}
