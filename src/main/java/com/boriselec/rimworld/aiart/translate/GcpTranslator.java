package com.boriselec.rimworld.aiart.translate;

import com.boriselec.rimworld.aiart.Counters;
import com.boriselec.rimworld.aiart.data.ArtDescription;
import com.boriselec.rimworld.aiart.generator.GcpClient;
import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.TranslationServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component
@ConditionalOnProperty("gcp.project")
public class GcpTranslator implements Translator {
    private static final Language TARGET_LANG = Language.ENGLISH;

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final TranslationServiceClient client;
    private final LocationName apiLocation;
    private final Counters counters;
    private final Map<String, String> thingDescCache;

    private final AtomicReference<SimpleImmutableEntry<ArtDescription, ArtDescription>> cachedLastQuery;

    public GcpTranslator(TranslationServiceClient client, GcpClient.GcpInstance gcpInstance, Counters counters,
                         Map<String, String> thingDescCache) {
        this.client = client;
        this.counters = counters;
        this.apiLocation = LocationName.of(gcpInstance.project(), "global");
        this.thingDescCache = thingDescCache;
        this.cachedLastQuery = new AtomicReference<>();
    }

    public ArtDescription translateFrom(Language language, ArtDescription description) {
        if (language == TARGET_LANG) {
            return description;
        }
        log.info("Cached last rq: " + cachedLastQuery.get());
        Optional<ArtDescription> cached = Optional.ofNullable(cachedLastQuery.get())
                .filter(c -> c.getKey().equals(description))
                .map(SimpleImmutableEntry::getValue);
        if (cached.isPresent()) {
            return cached.get();
        }

        ArtDescription translated = new ArtDescription(
                translateFrom(language, description.artDesc()),
                translateFromCached(language, description.thingDesc(), thingDescCache));
        cachedLastQuery.set(new SimpleImmutableEntry<>(description, translated));
        return translated;
    }

    private String translateFrom(Language language, String description) {
        if (description.isEmpty()) {
            return description;
        }
        try {
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

            String translatedText = response.getTranslations(0).getTranslatedText();
            log.info("Translated %s -> %s".formatted(description, translatedText));
            return translatedText;
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            return description;
        }
    }

    private String translateFromCached(Language language, String description, Map<String, String> cache) {
        if (cache.containsKey(description)) {
            return cache.get(description);
        }
        String result = translateFrom(language, description);
        cache.put(description, result);
        return result;
    }
}
