package com.stars.bigbang.repository;

import com.stars.bigbang.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying(clearAutomatically = true)
    @Query(value = "update User u set u.tokenVersion = u.tokenVersion + 1 where u.id = :userId")
    void incrementTokenVersionByUserId(long userId);
}
