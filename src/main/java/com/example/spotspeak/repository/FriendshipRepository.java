package com.example.spotspeak.repository;

import com.example.spotspeak.entity.Friendship;
import com.example.spotspeak.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends CrudRepository<Friendship, Long> {
    @Query("SELECT f FROM Friendship f WHERE f.userInitiating = :user OR f.userReceiving = :user")
    List<Friendship> findAllByUser(@Param("user") User user);

    @Query("SELECT f FROM Friendship f WHERE (f.userInitiating = :user1 AND f.userReceiving = :user2) OR (f.userInitiating = :user2 AND f.userReceiving = :user1)")
    Optional<Friendship> findByUsers(@Param("user1") User user1, @Param("user2") User user2);
}
