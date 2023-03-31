package com.boriselec.rimworld.aiart.translate;

import com.boriselec.rimworld.aiart.Counters;
import com.boriselec.rimworld.aiart.data.ArtDescription;
import com.boriselec.rimworld.aiart.generator.GcpClient;
import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.TranslationServiceClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Optional;

@Component
@ConditionalOnProperty("gcp.project")
public class GcpTranslator implements Translator {
    private static final Language TARGET_LANG = Language.ENGLISH;

    private final TranslationServiceClient client;
    private final LocationName apiLocation;
    private final Counters counters;

    private SimpleImmutableEntry<ArtDescription, ArtDescription> cachedLastQuery;

    public GcpTranslator(TranslationServiceClient client, GcpClient.GcpInstance gcpInstance, Counters counters) {
        this.client = client;
        this.counters = counters;
        this.apiLocation = LocationName.of(gcpInstance.project(), "global");
    }

    public ArtDescription translateFrom(Language language, ArtDescription description) {
        if (language == TARGET_LANG) {
            return description;
        }
        Optional<ArtDescription> cached = Optional.ofNullable(cachedLastQuery)
                .filter(c -> c.getKey().equals(description))
                .map(SimpleImmutableEntry::getValue);
        if (cached.isPresent()) {
            return cached.get();
        }

        ArtDescription translated = new ArtDescription(
                translateFrom(language, description.artDesc()),
                translateFrom(language, description.thingDesc()));
        cachedLastQuery = new SimpleImmutableEntry<>(description, translated);
        return translated;
    }

    private String translateFrom(Language language, String description) {
        TranslateTextRequest request =
                TranslateTextRequest.newBuilder()
                        .setParent(apiLocation.toString())
                        .setMimeType("text/plain")
                        .setSourceLanguageCode(language.getIso639Code())
                        .setTargetLanguageCode(TARGET_LANG.getIso639Code())
                        .addContents(description)
                        .build();
        TranslateTextResponse response = client.translateText(request);
        counters.translatedChars().increment(description.length());

        return response.getTranslations(0).getTranslatedText();
    }
}
