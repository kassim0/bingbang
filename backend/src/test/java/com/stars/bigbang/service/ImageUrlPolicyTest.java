package com.stars.bigbang.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ImageUrlPolicyTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "https://images.igdb.com/igdb/image/upload/t_cover_big/co2lbd.jpg",
            "https://media.rawg.io/media/games/456/456dea5e1c7e3cd07060c14e96612001.jpg",
            "https://IMAGES.IGDB.COM/igdb/image/upload/t_cover_big/co2lbd.jpg"
    })
    void imagesFromOurGameSourcesAreKept(String url) {
        assertEquals(url, ImageUrlPolicy.trustedOrNull(url));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "   ",
            "https://evil.example/track.png",
            "http://images.igdb.com/not-https.jpg",
            "https://images.igdb.com.evil.example/fake.jpg",
            "https://evil.example/?url=https://images.igdb.com/a.jpg",
            "javascript:alert(1)",
            "//images.igdb.com/no-scheme.jpg",
            "not a url"
    })
    void anythingElseIsDropped(String url) {
        assertNull(ImageUrlPolicy.trustedOrNull(url));
    }

    @Test
    void surroundingSpacesAreRemoved() {
        assertEquals("https://images.igdb.com/a.jpg", ImageUrlPolicy.trustedOrNull("  https://images.igdb.com/a.jpg "));
    }
}
