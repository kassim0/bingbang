package com.stars.bigbang.dto.payload;

import jakarta.validation.constraints.NotBlank;

/**
 * @param background nom du fond choisi, ex. "AURORA". Reçu comme texte (et non comme enum) pour pouvoir
 *                   répondre un 400 clair si le nom est inconnu.
 */
public record WalletBackgroundDto(@NotBlank String background) {
}
