package com.stars.bigbang.dto.payload;

import com.stars.bigbang.entity.ShareScope;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * @param scope               WALLET (tout le wallet) ou LISTS (les listes de listIds)
 * @param listIds             listes à partager ; obligatoire et non vide pour LISTS, ignoré pour WALLET
 * @param includeCommentaries true pour montrer aussi les commentaires (non par défaut)
 */
public record CreateShareLinkDto(
        @NotNull ShareScope scope,
        @Size(max = 100) List<@NotNull Long> listIds,
        Boolean includeCommentaries
) {
}
