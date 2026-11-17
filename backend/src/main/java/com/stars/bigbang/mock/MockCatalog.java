package com.stars.bigbang.mock;

import java.util.List;
import java.util.Locale;

/**
 * Petit catalogue de jeux utilisé par le profil "mock" (développement et tests e2e sans clé d'API).
 * Les deux mocks (IGDB et RAWG) filtrent ce catalogue avec le texte recherché, comme le ferait une vraie
 * source : chercher "zelda" ne renvoie donc pas les mêmes jeux que chercher "mario".
 */
final class MockCatalog {

    /**
     * @param imageId identifiant de jaquette au format IGDB. Seuls les trois premiers sont de vrais identifiants,
     *                les autres sont fictifs : leur image ne s'affichera pas.
     */
    record MockGame(int id, String slug, String name, String released, long releasedTimestamp, String imageId) {
    }

    static final List<MockGame> GAMES = List.of(
            new MockGame(1020, "grand-theft-auto-v", "Grand Theft Auto V", "2013-09-17", 1379376000L, "co2lbd"),
            new MockGame(1942, "the-witcher-3-wild-hunt", "The Witcher 3: Wild Hunt", "2015-05-19", 1431993600L, "coaarl"),
            new MockGame(72, "portal-2", "Portal 2", "2011-04-19", 1303171200L, "co1rs4"),
            new MockGame(7346, "the-legend-of-zelda-breath-of-the-wild", "The Legend of Zelda: Breath of the Wild", "2017-03-03", 1488499200L, "mock01"),
            new MockGame(119388, "the-legend-of-zelda-tears-of-the-kingdom", "The Legend of Zelda: Tears of the Kingdom", "2023-05-12", 1683849600L, "mock02"),
            new MockGame(26758, "super-mario-odyssey", "Super Mario Odyssey", "2017-10-27", 1509062400L, "mock03"),
            new MockGame(1070, "super-mario-world", "Super Mario World", "1990-11-21", 659145600L, "mock04"),
            new MockGame(11737, "outer-wilds", "Outer Wilds", "2019-05-28", 1559001600L, "mock05"),
            new MockGame(113112, "hades", "Hades", "2020-09-17", 1600300800L, "mock06"),
            new MockGame(26226, "celeste", "Celeste", "2018-01-25", 1516838400L, "mock07"),
            new MockGame(14593, "hollow-knight", "Hollow Knight", "2017-02-24", 1487894400L, "mock08"),
            new MockGame(17000, "stardew-valley", "Stardew Valley", "2016-02-26", 1456444800L, "mock09")
    );

    private MockCatalog() {
    }

    /** Jeux dont le nom contient le texte recherché, sans tenir compte des majuscules. */
    static List<MockGame> search(String text) {
        String wanted = text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
        return GAMES.stream()
                .filter(game -> game.name().toLowerCase(Locale.ROOT).contains(wanted))
                .toList();
    }
}
