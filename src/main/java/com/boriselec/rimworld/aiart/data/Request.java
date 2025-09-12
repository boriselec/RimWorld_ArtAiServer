package com.boriselec.rimworld.aiart.data;

import com.boriselec.rimworld.aiart.AiArtControllerV2.PromptRq;
import com.boriselec.rimworld.aiart.translate.Language;

public record Request(String artDesc, String thingDesc, Language language) {
    public static RequestWithUserId deserialize(String data) {
        String[] split = data.split(";");

        String artDesc = split[0];
        String thingDesc = split[1];
        String userId = split[2];
        Language language = split.length > 3
                ? Language.fromRimworldCode(split[3])
                : Language.ENGLISH;

        return new RequestWithUserId(userId, new Request(artDesc, thingDesc, language));
    }

    public static RequestWithUserId deserializeV2(PromptRq rq) {
        PromptRq.PromptRqData rqData = rq.artAi();
        Language language = Language.fromRimworldCode(rqData.language());
        return new RequestWithUserId(
            rqData.userId(),
            new Request(
                rqData.artDescription(),
                rqData.thingDescription(),
                language));
    }

    public ArtDescription getArtDescription() {
        return new ArtDescription(artDesc, thingDesc);
    }
}
