package com.stars.bigbang.dto.payload;

import jakarta.validation.constraints.NotNull;

/**
 * @param targetListId liste qui reçoit une copie du commentaire
 * @param replace      true pour remplacer le commentaire que la liste cible a déjà sur ce jeu ;
 *                     sinon (false ou absent) la copie est refusée dans ce cas
 */
public record CommentaryTransferDto(@NotNull Long targetListId, Boolean replace) {
}
