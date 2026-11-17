package com.stars.bigbang.dto.response;

import com.stars.bigbang.entity.ShareScope;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Un lien de partage, vu par son propriétaire.
 *
 * @param listNames noms des listes encore existantes de ce lien (vide pour la portée WALLET)
 */
public record ShareLinkDto(long id, String token, ShareScope scope, boolean includeCommentaries,
                           List<String> listNames, LocalDateTime createdAt) {
}
