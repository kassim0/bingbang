package com.stars.bigbang.service;

import com.stars.bigbang.dto.response.WalletDto;
import com.stars.bigbang.entity.Wallet;
import com.stars.bigbang.entity.WalletBackground;
import com.stars.bigbang.repository.UserRepository;
import com.stars.bigbang.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    /** Le wallet n'est créé en base qu'au premier changement : d'ici là, on répond avec le fond par défaut. */
    @Transactional(readOnly = true)
    public WalletDto getWallet(long userId) {
        return new WalletDto(backgroundOf(userId));
    }

    @Transactional(readOnly = true)
    public WalletBackground backgroundOf(long userId) {
        return walletRepository.findByUserId(userId).map(Wallet::getBackground).orElse(WalletBackground.DEFAULT);
    }

    @Transactional
    public WalletDto setBackground(long userId, String backgroundName) {
        WalletBackground background = parse(backgroundName);
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseGet(() -> new Wallet(userRepository.getReferenceById(userId)));
        wallet.setBackground(background);
        walletRepository.save(wallet);
        return new WalletDto(background);
    }

    private WalletBackground parse(String name) {
        try {
            return WalletBackground.valueOf(name.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown background");
        }
    }
}
