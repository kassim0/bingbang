package com.stars.bigbang.repository;

import com.stars.bigbang.entity.GameList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GameListRepository extends JpaRepository<GameList,Long> {

    @Query("SELECT COALESCE(MAX(g.listOrder), 0) FROM GameList g")
    Long findMaxListOrder();
}
