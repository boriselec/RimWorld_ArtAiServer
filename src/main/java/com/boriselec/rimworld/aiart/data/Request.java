package com.boriselec.rimworld.aiart.data;

public record Request (String artDesc, String thingDesc) {
    public static Request deserialize(String data) {
        String[] split = data.split(";");
        return new Request(split[0], split[1]);
    }
}
