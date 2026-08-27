package com.stars.bigbang.repository;

import com.stars.bigbang.entity.GamesList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GameListRepository extends JpaRepository<GamesList,Long> {

    @Query("SELECT COALESCE(MAX(g.position), 0) FROM GamesList g")
    Long findMaxOrder();
}
