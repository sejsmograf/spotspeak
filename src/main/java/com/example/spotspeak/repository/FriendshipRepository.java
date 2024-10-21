package com.example.spotspeak.repository;

import com.example.spotspeak.entity.Friendship;
import com.example.spotspeak.entity.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FriendshipRepository extends CrudRepository<Friendship, Long> {
    @Query("SELECT f FROM Friendship f WHERE f.userInitiating = :user OR f.userReceiving = :user")
    List<Friendship> findAllByUser(@Param("user") User user);

    @Modifying
    @Query("DELETE FROM Friendship f WHERE f.userInitiating = :user OR f.userReceiving = :user")
    void deleteAllFriendshipsByUser(@Param("user") User user);
}
