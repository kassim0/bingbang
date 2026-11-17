package com.stars.bigbang.dto.rawgDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * Réponse de RAWG à une recherche. RAWG renvoie beaucoup d'autres champs (pagination, SEO...) :
 * on ne déclare que ceux qu'on utilise, les autres sont ignorés grâce à ignoreUnknown.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RawgResponseDto {

    private int count;
    private List<RawgResultsDto> results;
}
