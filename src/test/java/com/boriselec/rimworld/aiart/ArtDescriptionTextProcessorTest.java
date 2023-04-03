package com.boriselec.rimworld.aiart;

import com.boriselec.rimworld.aiart.data.ArtDescription;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ArtDescriptionTextProcessorTest {
    @Test
    public void testArtDateClear() {
        String artDesc = "This representation tells the story of Mayrén completing work on a machine pistol on 13th of Septober, 5502.";
        String thingDesc = "A torso-sized piece of material sculpted into an artistic form.";
        ArtDescription request = new ArtDescription(artDesc, thingDesc);

        ArtDescription description = ArtDescriptionTextProcessor.process(request);

        Assertions.assertEquals(new ArtDescription(
                        "This representation tells the story of Mayrén completing work on a machine pistol.",
                        "A torso-sized piece of material sculpted into an artistic form."),
                description);
    }

    @Test
    public void testArtDateRussianClear() {
        String artDesc = "Резьба на этом предмете изображает, как возводится буровая установка удивительной красоты. Изображение этого творения рук человеческих поражает своей детализацией. Картину окаймляет тройка изображений. На них— ворон. Каким-то образом изображению удаётся одновременно передать и идею уныния, и идею бога. Эта работа относится к тому, как Мисси заканчивает строить буровая установка 9 Мартомай 5503 г.";
        String thingDesc = "Уютный матрац и простыни на прочной раме, для комфортного отдыха.";
        ArtDescription request = new ArtDescription(artDesc, thingDesc);

        ArtDescription description = ArtDescriptionTextProcessor.process(request);

        Assertions.assertEquals(new ArtDescription(
                        "Резьба на этом предмете изображает, как возводится буровая установка удивительной красоты. Изображение этого творения рук человеческих поражает своей детализацией. Картину окаймляет тройка изображений. На них— ворон. Каким-то образом изображению удаётся одновременно передать и идею уныния, и идею бога. Эта работа относится к тому, как Мисси заканчивает строить буровая установка.",
                        "Уютный матрац и простыни на прочной раме, для комфортного отдыха."),
                description);
    }

    @Test
    public void testTagClear() {
        String artDesc = "test.";
        String thingDesc = "test.<i><color=#66E0E4FF>Roo's Painting Expansion</color></i>";
        ArtDescription request = new ArtDescription(artDesc, thingDesc);

        ArtDescription description = ArtDescriptionTextProcessor.process(request);

        Assertions.assertEquals(new ArtDescription("test.", "test."), description);
    }

    @Test
    public void testLineBreakClear() {
        String artDesc = "test.";
        String thingDesc = "test.\n\n test.";
        ArtDescription request = new ArtDescription(artDesc, thingDesc);

        ArtDescription description = ArtDescriptionTextProcessor.process(request);

        Assertions.assertEquals(new ArtDescription("test.", "test. test."), description);
    }

    @Test
    public void testFurnitureModClear() {
        String artDesc = "This sculpture shows Lada Ballard trying to light a fire and shivering uncontrollably. A cold blue moon looms in the background.";
        String thingDesc = "A small-size piece of material on a decorative pedestal, sculpted into an artistic form. (Vanilla Furniture Expanded - Art) ";
        ArtDescription request = new ArtDescription(artDesc, thingDesc);

        ArtDescription description = ArtDescriptionTextProcessor.process(request);

        Assertions.assertEquals(new ArtDescription(
                        "This sculpture shows Lada Ballard trying to light a fire and shivering uncontrollably. A cold blue moon looms in the background.",
                        "A small-size piece of material on a decorative pedestal, sculpted into an artistic form."),
                description);
    }

    @Test
    public void testDresserUnrelatedClear() {
        String artDesc = "An engraving on this furniture is shaped like Barry Wollertsen cupping Caraleigh Tuzii's chin with a sense of tenderness. Caraleigh shyly covers Barry's eyes. The work symbolizes debt. Provocatively, three shamans appear in the distance. This image relates to Barry's kiss with Caraleigh.";
        String thingDesc = "A dresser. Gives a small comfort bonus to all nearby beds. Placing more than one dresser near the same bed has no effect.";
        ArtDescription request = new ArtDescription(artDesc, thingDesc);

        ArtDescription description = ArtDescriptionTextProcessor.process(request);

        Assertions.assertEquals(new ArtDescription(
                        "An engraving on this furniture is shaped like Barry Wollertsen cupping Caraleigh Tuzii's chin with a sense of tenderness. Caraleigh shyly covers Barry's eyes. The work symbolizes debt. Provocatively, three shamans appear in the distance. This image relates to Barry's kiss with Caraleigh.",
                        "A dresser. Gives a small comfort bonus to all nearby beds."),
                description);
    }

    @Test
    public void testColourTagClear() {
        String artDesc = "<color=#9f40ff>bloodlust</color> <color=#9f40ff>cannibal</color> <color=#d4af37>asexual</color> woman Trained Ghoul light-skinned with shoulder-length blond hair in blue clothes age 25";
        String thingDesc = "beautiful portrait of a human";
        ArtDescription request = new ArtDescription(artDesc, thingDesc);

        ArtDescription description = ArtDescriptionTextProcessor.process(request);

        Assertions.assertEquals(new ArtDescription(
                        "bloodlust cannibal asexual woman Trained Ghoul light-skinned with shoulder-length blond hair in blue clothes age 25",
                        "beautiful portrait of a human"),
                description);
    }

    @Test
    public void testTalentClear() {
        String artDesc = "thin lazy Priest <color=#9f40ff>Talent: Chef</color> male crashbaby dark-skinned with shoulder-length brunette hair clean-shaven in teal clothes age 18";
        String thingDesc = "beautiful portrait of a human";
        ArtDescription request = new ArtDescription(artDesc, thingDesc);

        ArtDescription description = ArtDescriptionTextProcessor.process(request);

        Assertions.assertEquals(new ArtDescription(
                        "thin lazy Priest Chef male crashbaby dark-skinned with shoulder-length brunette hair clean-shaven in teal clothes age 18",
                        "beautiful portrait of a human"),
                description);
    }
}