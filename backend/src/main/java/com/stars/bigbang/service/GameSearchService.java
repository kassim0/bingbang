package com.stars.bigbang.service;

import com.stars.bigbang.dto.response.GameSearchResult;
import com.stars.bigbang.entity.AppSetting;
import com.stars.bigbang.entity.GameSource;
import com.stars.bigbang.repository.AppSettingRepository;
import com.stars.bigbang.service.provider.GameProvider;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
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

    /** Nombre maximum de recherches gardées en mémoire : au-delà, les plus anciennes sont oubliées. */
    private static final int CACHE_MAX_ENTRIES = 1000;

    private final AppSettingRepository appSettingRepository;
    private final Map<GameSource, GameProvider> providers;

    /**
     * Résultats des dernières recherches, par source et par texte cherché.
     * Une recherche déjà faite récemment est servie depuis la mémoire, sans rappeler IGDB/RAWG :
     * la réponse est immédiate et le quota des API est préservé.
     */
    private final Cache<String, List<GameSearchResult>> cache;

    /** Sans cache : chaque recherche appelle la source. Utilisé par les tests qui vérifient ces appels. */
    public GameSearchService(AppSettingRepository appSettingRepository, List<GameProvider> providers) {
        this(appSettingRepository, providers, Duration.ZERO);
    }

    /**
     * @param cacheDuration durée pendant laquelle une recherche reste en mémoire (propriété
     *                      bingbang.search.cache-duration, 10 minutes par défaut ; 0 désactive le cache)
     */
    @Autowired
    public GameSearchService(AppSettingRepository appSettingRepository,
                             List<GameProvider> providers,
                             @Value("${bingbang.search.cache-duration:10m}") Duration cacheDuration) {
        this.appSettingRepository = appSettingRepository;
        this.providers = providers.stream().collect(Collectors.toMap(GameProvider::source, Function.identity()));
        this.cache = Caffeine.newBuilder()
                .maximumSize(CACHE_MAX_ENTRIES)
                .expireAfterWrite(cacheDuration)
                .build();
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
        String searchedText = normalize(name);
        String cacheKey = source + "|" + searchedText.toLowerCase(Locale.ROOT);
        try {
            // get(clé, calcul) : renvoie la valeur en mémoire, ou lance le calcul et le retient.
            // Si la source échoue, l'exception remonte et rien n'est mis en cache.
            return cache.get(cacheKey, key -> providers.get(source).search(searchedText));
        } catch (FeignException e) {
            // On ne journalise pas l'exception : son message contient l'URL appelée, donc la clé RAWG.
            log.error("Échec de la recherche sur {} (HTTP {})", source, e.status());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "La source de jeux " + source + " ne répond pas");
        }
    }

    /** "  The   Witcher " devient "The Witcher" : deux saisies équivalentes partagent la même entrée du cache. */
    private String normalize(String name) {
        return name.strip().replaceAll("\\s+", " ");
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
