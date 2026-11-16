package com.stars.bigbang.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

/**
 * Décide si une URL d'image reçue du navigateur peut être enregistrée.
 * <p>
 * La table des jeux est commune à tous les utilisateurs : sans ce contrôle, n'importe qui pourrait
 * y enregistrer une image hébergée sur son propre serveur, que le navigateur des autres utilisateurs
 * irait ensuite charger. On n'accepte donc que les serveurs d'images de nos sources de jeux.
 */
public final class ImageUrlPolicy {

    /** Serveurs d'images d'IGDB et de RAWG. À compléter si une nouvelle source de jeux est ajoutée. */
    private static final Set<String> TRUSTED_HOSTS = Set.of("images.igdb.com", "media.rawg.io");

    private ImageUrlPolicy() {
    }

    /** Renvoie l'URL si elle est en https et pointe vers un serveur d'images connu, sinon null (pas d'image). */
    public static String trustedOrNull(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        try {
            URI uri = new URI(imageUrl.trim());
            String host = uri.getHost();
            boolean trusted = "https".equalsIgnoreCase(uri.getScheme())
                    && host != null
                    && TRUSTED_HOSTS.contains(host.toLowerCase(Locale.ROOT));
            return trusted ? uri.toString() : null;
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
