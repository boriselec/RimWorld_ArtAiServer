package com.boriselec.rimworld.aiart.data;

import com.boriselec.rimworld.aiart.AiArtControllerV2.PromptRq;
import com.boriselec.rimworld.aiart.generator.Model;

import java.util.UUID;

import static java.util.Optional.ofNullable;

public record Request(String prompt, Model model) {
    public static Request deserialize(String data) {
        String[] split = data.split(";");

        String artDesc = split[0];
        String thingDesc = split[1];
        String description = thingDesc + " " + artDesc;

        return new Request(description, Model.DEFAULT);
    }

    public static Request deserializeV2(PromptRq rq, Model modelNullable) {
        Model model = ofNullable(modelNullable).orElse(Model.DEFAULT);
        return new Request(rq.prompt(), model);
    }

    public String uid() {
        String promptUid = UUID.nameUUIDFromBytes(prompt.getBytes()).toString();
        return model.name() + "_" + promptUid;
    }
}
