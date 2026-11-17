package com.stars.bigbang.dto.response;

import com.stars.bigbang.entity.GameCommentary;

public record CommentaryDto(long gamesListId, long gameId, String text) {

    public static CommentaryDto from(GameCommentary commentary) {
        return new CommentaryDto(commentary.getGamesList().getId(), commentary.getGame().getId(), commentary.getText());
    }
}
