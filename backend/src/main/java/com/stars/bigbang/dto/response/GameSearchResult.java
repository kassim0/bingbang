package com.stars.bigbang.dto.response;

import com.stars.bigbang.entity.GameSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Résultat de recherche commun à toutes les sources.
 * Un externalId n'a de sens qu'avec sa source : le même nombre peut désigner deux jeux différents.
 * <p>
 * Le navigateur renvoie ces mêmes objets quand il ajoute des jeux à une liste : les contraintes
 * ci-dessous sont vérifiées à ce moment-là (@Valid dans le contrôleur), car on ne peut pas faire
 * confiance à ce qui vient du navigateur.
 */
public record GameSearchResult(
        @NotNull GameSource source,
        @NotNull @Positive Integer externalId,
        @Size(max = 255) String slug,
        @NotBlank @Size(max = 255) String name,
        LocalDate releaseDate,
        @Size(max = 500) String imageUrl
) {
}
