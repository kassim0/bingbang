package com.stars.bigbang.service.provider;

import com.stars.bigbang.dto.response.GameSearchResult;
import com.stars.bigbang.entity.GameSource;

import java.util.List;

/**
 * Une source de jeux. Pour en ajouter une : implémenter cette interface et ajouter sa valeur à GameSource.
 */
public interface GameProvider {

    GameSource source();

    List<GameSearchResult> search(String name);
}
