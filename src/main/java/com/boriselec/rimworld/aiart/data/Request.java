package com.boriselec.rimworld.aiart.data;

import com.boriselec.rimworld.aiart.AiArtControllerV2.PromptRq;
import com.boriselec.rimworld.aiart.translate.Language;

public record Request(String prompt, Language language) {
    public static Request deserialize(String data) {
        String[] split = data.split(";");

        String artDesc = split[0];
        String thingDesc = split[1];
        String description = thingDesc + " " + artDesc;
        Language language = split.length > 3
            ? Language.fromRimworldCode(split[3])
            : Language.ENGLISH;

        return new Request(description, language);
    }

    public static Request deserializeV2(PromptRq rq) {
        Language language = Language.fromRimworldCode(rq.language());
        return new Request(rq.prompt(), language);
    }
}
