package com.stars.bigbang.controller;

import com.stars.bigbang.dto.payload.CreateShareLinkDto;
import com.stars.bigbang.dto.response.ShareLinkDto;
import com.stars.bigbang.dto.response.SharedWalletDto;
import com.stars.bigbang.security.UserPrincipal;
import com.stars.bigbang.service.ShareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @PostMapping
    public ResponseEntity<ShareLinkDto> create(@Valid @RequestBody CreateShareLinkDto request,
                                               @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shareService.create(principal.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<ShareLinkDto>> findMine(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(shareService.findMine(principal.getId()));
    }

    @DeleteMapping("/{linkId}")
    public ResponseEntity<Void> revoke(@PathVariable long linkId, @AuthenticationPrincipal UserPrincipal principal) {
        shareService.revoke(principal.getId(), linkId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lecture d'un lien de partage. N'importe quel utilisateur connecté peut l'appeler (un invité aussi) :
     * c'est le token du lien qui donne le droit de voir, pas l'identité du visiteur.
     * Aujourd'hui la configuration de sécurité exige quand même un utilisateur connecté pour arriver ici ;
     * voir docs/ROADMAP.md (phase 4) pour rendre cette route réellement publique.
     */
    @GetMapping("/view/{token}")
    public ResponseEntity<SharedWalletDto> view(@PathVariable String token) {
        return ResponseEntity.ok(shareService.view(token));
    }
}
