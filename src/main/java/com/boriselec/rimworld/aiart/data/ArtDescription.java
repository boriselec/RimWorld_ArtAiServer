package com.boriselec.rimworld.aiart.data;

import java.util.UUID;

public record ArtDescription(String artDesc, String thingDesc) {
    public String toString() {
        return thingDesc + " " + artDesc;
    }

    public String uid() {
        return UUID.nameUUIDFromBytes(toString().getBytes()).toString();
    }
}
