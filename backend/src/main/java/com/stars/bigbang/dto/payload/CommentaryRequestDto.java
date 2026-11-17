package com.stars.bigbang.dto.payload;

import com.stars.bigbang.entity.GameCommentary;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentaryRequestDto(
        @NotBlank @Size(max = GameCommentary.TEXT_MAX_LENGTH) String text
) {
}
