package com.stars.bigbang.service;

import com.stars.bigbang.dto.response.GameSearchResult;
import com.stars.bigbang.entity.AppSetting;
import com.stars.bigbang.entity.GameSource;
import com.stars.bigbang.repository.AppSettingRepository;
import com.stars.bigbang.service.provider.GameProvider;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Lance les recherches sur la source active, définie par la ligne game_provider de la table app_setting.
 * Pour basculer, sans redémarrage : UPDATE app_setting SET setting_value = 'RAWG' WHERE setting_key = 'game_provider';
 */
@Service
@Slf4j
public class GameSearchService {

    static final String PROVIDER_SETTING = "game_provider";
    static final GameSource DEFAULT_SOURCE = GameSource.IGDB;

    private final AppSettingRepository appSettingRepository;
    private final Map<GameSource, GameProvider> providers;

    public GameSearchService(AppSettingRepository appSettingRepository, List<GameProvider> providers) {
        this.appSettingRepository = appSettingRepository;
        this.providers = providers.stream().collect(Collectors.toMap(GameProvider::source, Function.identity()));
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createDefaultSettingIfMissing() {
        if (!appSettingRepository.existsById(PROVIDER_SETTING)) {
            appSettingRepository.save(new AppSetting(PROVIDER_SETTING, DEFAULT_SOURCE.name()));
        }
    }

    public List<GameSearchResult> search(String name) {
        if (name == null || name.isBlank()) {
            return List.of();
        }
        GameSource source = activeSource();
        try {
            return providers.get(source).search(name);
        } catch (FeignException e) {
            // On ne journalise pas l'exception : son message contient l'URL appelée, donc la clé RAWG.
            log.error("Échec de la recherche sur {} (HTTP {})", source, e.status());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "La source de jeux " + source + " ne répond pas");
        }
    }

    GameSource activeSource() {
        String value = appSettingRepository.findById(PROVIDER_SETTING).map(AppSetting::getValue).orElse(DEFAULT_SOURCE.name());
        try {
            return GameSource.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Valeur inconnue pour {} : '{}', utilisation de {}", PROVIDER_SETTING, value, DEFAULT_SOURCE);
            return DEFAULT_SOURCE;
        }
    }
}
