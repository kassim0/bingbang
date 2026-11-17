package com.stars.bigbang.controller;

import com.stars.bigbang.dto.payload.CommentaryRequestDto;
import com.stars.bigbang.dto.payload.CommentaryTransferDto;
import com.stars.bigbang.dto.response.CommentaryDto;
import com.stars.bigbang.security.UserPrincipal;
import com.stars.bigbang.service.CommentaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game/gamesList/{listId}/games/{gameId}/commentary")
@RequiredArgsConstructor
public class CommentaryController {

    private final CommentaryService commentaryService;

    @PutMapping
    public ResponseEntity<CommentaryDto> save(@PathVariable long listId, @PathVariable long gameId,
                                              @Valid @RequestBody CommentaryRequestDto request,
                                              @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(commentaryService.save(listId, gameId, request.text(), principal.getId()));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@PathVariable long listId, @PathVariable long gameId,
                                       @AuthenticationPrincipal UserPrincipal principal) {
        commentaryService.delete(listId, gameId, principal.getId());
        return ResponseEntity.noContent().build();
    }

    /** Copie le commentaire vers une autre liste (le « transfert » du wiki). */
    @PostMapping("/transfer")
    public ResponseEntity<CommentaryDto> transfer(@PathVariable long listId, @PathVariable long gameId,
                                                  @Valid @RequestBody CommentaryTransferDto request,
                                                  @AuthenticationPrincipal UserPrincipal principal) {
        boolean replace = Boolean.TRUE.equals(request.replace());
        return ResponseEntity.ok(commentaryService.copyToList(listId, gameId, request.targetListId(), replace, principal.getId()));
    }
}
