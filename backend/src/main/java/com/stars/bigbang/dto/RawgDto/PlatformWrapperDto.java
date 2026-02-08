package com.stars.bigbang.dto.RawgDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PlatformWrapperDto {

    private PlatformDto platform;

    @JsonProperty("released_at")
    private String releasedAt;

    @JsonProperty("requirements_en")
    private RequirementsDto requirementsEn;

    @JsonProperty("requirements_ru")
    private RequirementsDto requirementsRu;

}
