package com.boriselec.rimworld.aiart.data;

import com.boriselec.rimworld.aiart.AiArtControllerV2.PromptRq;
import com.boriselec.rimworld.aiart.generator.Model;
import com.boriselec.rimworld.aiart.translate.Language;

import java.util.UUID;

import static java.util.Optional.ofNullable;

public record Request(String prompt, Language language, Model model) {
    public static Request deserialize(String data) {
        String[] split = data.split(";");

        String artDesc = split[0];
        String thingDesc = split[1];
        String description = thingDesc + " " + artDesc;
        Language language = split.length > 3
            ? Language.fromRimworldCode(split[3])
            : Language.ENGLISH;

        return new Request(description, language, Model.DEFAULT);
    }

    public static Request deserializeV2(PromptRq rq, Model modelNullable) {
        Language language = Language.fromRimworldCode(rq.language());
        Model model = ofNullable(modelNullable).orElse(Model.DEFAULT);
        return new Request(rq.prompt(), language, model);
    }

    public String uid() {
        String promptUid = UUID.nameUUIDFromBytes(prompt.getBytes()).toString();
        return model.name() + "_" + promptUid;
    }
}
