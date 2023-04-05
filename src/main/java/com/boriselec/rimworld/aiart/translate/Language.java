package com.boriselec.rimworld.aiart.translate;

import java.util.Arrays;

public enum Language {
    CATALAN("Catalan (Català)", "ca"),
    CHINESE_SIMPLIFIED("ChineseSimplified (简体中文)", "zh-CN"),
    CHINESE_TRADITIONAL("ChineseTraditional (繁體中文)", "zh-TW"),
    CZECH("Czech (Čeština)", "cs"),
    DANISH("Danish (Dansk)", "da"),
    DUTCH("Dutch (Nederlands)", "nl"),
    ENGLISH("English", "en"),
    ESTONIAN("Estonian (Eesti)", "et"),
    FINNISH("Finnish (Suomi)", "fi"),
    FRENCH("French (Français)", "fr"),
    GERMAN("German (Deutsch)", "de"),
    GREEK("Greek (Ελληνικά)", "el"),
    HUNGARIAN("Hungarian (Magyar)", "hu"),
    ITALIAN("Italian (Italiano)", "it"),
    JAPANESE("Japanese (日本語)", "ja"),
    KOREAN("Korean (한국어)", "ko"),
    NORWEGIAN("Norwegian (Norsk Bokmål)", "no"),
    POLISH("Polish (Polski)", "pl"),
    PORTUGUESE("Portuguese (Português)", "pt"),
    PORTUGUESE_BRAZILIAN("PortugueseBrazilian (Português Brasileiro)", "pt"),
    ROMANIAN("Romanian (Română)", "ro"),
    RUSSIAN("Russian (Русский)", "ru"),
    SLOVAK("Slovak (Slovenčina)", "sk"),
    SPANISH("Spanish (Español(Castellano))", "es"),
    SPANISH_LATIN("SpanishLatin (Español(Latinoamérica))", "es"),
    SWEDISH("Swedish (Svenska)", "sv"),
    TURKISH("Turkish (Türkçe)", "tr"),
    UKRAINIAN("Ukrainian (Українська)", "uk"),
    VIETNAMESE("Vietnamese", "vi"),
    ;

    /**
     * Language folder (Data/Core/Languages)
     */
    private final String rimworldCode;
    private final String iso639Code;

    Language(String rimworldCode, String iso639Code) {
        this.rimworldCode = rimworldCode;
        this.iso639Code = iso639Code;
    }

    public static Language fromRimworldCode(String code) {
        return Arrays.stream(values())
                .filter(l -> l.rimworldCode.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported language: " + code));
    }

    public String getIso639Code() {
        return iso639Code;
    }
}
