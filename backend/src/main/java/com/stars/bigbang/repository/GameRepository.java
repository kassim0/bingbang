package com.stars.bigbang.repository;

import com.stars.bigbang.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    Optional<Game> findByRawgId(Integer rawgId);

    Optional<Game> findBySlug(String slug);

}
