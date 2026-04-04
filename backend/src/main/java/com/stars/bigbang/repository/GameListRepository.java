package com.stars.bigbang.repository;

import com.stars.bigbang.entity.GameList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameListRepository extends JpaRepository<GameList, Long> {

    List<GameList> findByUserId(Long userId);

    List<GameList> findByDeviceId(String deviceId);

}
