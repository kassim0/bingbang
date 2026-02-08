package com.stars.bigbang.dto.RawgDto;

import lombok.Data;
import java.util.List;

@Data
public class RawgResponseDto {

    private Integer count;
    private String next;
    private String previous;
    private List<RawgResultsDto> results;

}
