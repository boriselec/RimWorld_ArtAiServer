package com.boriselec.rimworld.aiart.translate;

import org.springframework.util.StringUtils;

import java.util.Arrays;

public enum Language {
    CATALAN("Catalan (Català)", "Catalan", "ca"),
    CHINESE_SIMPLIFIED("ChineseSimplified (简体中文)", "Chinese", "zh-CN"),
    CHINESE_TRADITIONAL("ChineseTraditional (繁體中文)", null, "zh-TW"),
    CZECH("Czech (Čeština)", "Czech", "cs"),
    DANISH("Danish (Dansk)", "Danish", "da"),
    DUTCH("Dutch (Nederlands)", "Dutch", "nl"),
    ENGLISH("English", "English", "en"),
    ESTONIAN("Estonian (Eesti)", "Estonian", "et"),
    FINNISH("Finnish (Suomi)", "Finnish", "fi"),
    FRENCH("French (Français)", "French", "fr"),
    GERMAN("German (Deutsch)", "German", "de"),
    GREEK("Greek (Ελληνικά)", "Greek", "el"),
    HUNGARIAN("Hungarian (Magyar)", "Hungarian", "hu"),
    ITALIAN("Italian (Italiano)", "Italian", "it"),
    JAPANESE("Japanese (日本語)", "Japanese", "ja"),
    KOREAN("Korean (한국어)", "Korean", "ko"),
    NORWEGIAN("Norwegian (Norsk Bokmål)", "Norwegian", "no"),
    POLISH("Polish (Polski)", "Polish", "pl"),
    PORTUGUESE("Portuguese (Português)", "Portuguese", "pt"),
    PORTUGUESE_BRAZILIAN("PortugueseBrazilian (Português Brasileiro)", null, "pt"),
    ROMANIAN("Romanian (Română)", "Romanian", "ro"),
    RUSSIAN("Russian (Русский)", "Russian", "ru"),
    RUSSIAN_SK(null, "Russian-SK", "ru"),
    SLOVAK("Slovak (Slovenčina)", "Slovak", "sk"),
    SPANISH("Spanish (Español(Castellano))", "Spanish", "es"),
    SPANISH_LATIN("SpanishLatin (Español(Latinoamérica))", null, "es"),
    SWEDISH("Swedish (Svenska)", "Swedish", "sv"),
    TURKISH("Turkish (Türkçe)", "Turkish", "tr"),
    UKRAINIAN("Ukrainian (Українська)", "Ukrainian", "uk"),
    VIETNAMESE(null, "Vietnamese", "vi"),
    ;

    /**
     * Language folder (Data/Core/Languages)
     */
    private final String rimworldCode;
    private final String englishName;
    private final String iso639Code;

    Language(String rimworldCode, String englishName, String iso639Code) {
        this.rimworldCode = rimworldCode;
        this.englishName = englishName;
        this.iso639Code = iso639Code;
    }

    public static Language fromRimworldCode(String code) {
        if (!StringUtils.hasText(code)) {
            return ENGLISH;
        }
        return Arrays.stream(values())
                .filter(lang -> code.equals(lang.rimworldCode) || code.equals(lang.englishName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported language: " + code));
    }

    public String getIso639Code() {
        return iso639Code;
    }
}
