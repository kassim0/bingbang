package com.stars.bigbang.repository;

import com.stars.bigbang.entity.ShareLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShareLinkRepository extends JpaRepository<ShareLink, Long> {

    Optional<ShareLink> findByToken(String token);

    List<ShareLink> findAllByUserIdOrderByIdDesc(Long userId);

    Optional<ShareLink> findByIdAndUserId(Long id, Long userId);
}
