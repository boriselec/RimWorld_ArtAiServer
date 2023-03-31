package com.boriselec.rimworld.aiart.data;

public record ArtDescription(String artDesc, String thingDesc) {
    public String toString() {
        return thingDesc + " " + artDesc;
    }
}
