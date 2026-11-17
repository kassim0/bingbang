package com.stars.bigbang.dto.response;

import com.stars.bigbang.entity.ShareScope;
import com.stars.bigbang.entity.WalletBackground;

import java.util.List;

/**
 * Ce que voit le visiteur d'un lien de partage. Volontairement minimal : le pseudo du propriétaire,
 * jamais son email ni son identifiant.
 */
public record SharedWalletDto(String ownerName, ShareScope scope, WalletBackground background, List<GamesListDto> lists) {
}
