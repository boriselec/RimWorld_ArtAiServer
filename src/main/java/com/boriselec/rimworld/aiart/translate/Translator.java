package com.boriselec.rimworld.aiart.translate;

import com.boriselec.rimworld.aiart.data.ArtDescription;

public interface Translator {
    ArtDescription translateFrom(Language language, ArtDescription description);
}
