package com.stars.bigbang.controller;

import com.stars.bigbang.dto.payload.WalletBackgroundDto;
import com.stars.bigbang.dto.response.WalletDto;
import com.stars.bigbang.security.UserPrincipal;
import com.stars.bigbang.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public ResponseEntity<WalletDto> getWallet(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(walletService.getWallet(principal.getId()));
    }

    @PutMapping("/background")
    public ResponseEntity<WalletDto> setBackground(@Valid @RequestBody WalletBackgroundDto request,
                                                   @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(walletService.setBackground(principal.getId(), request.background()));
    }
}
