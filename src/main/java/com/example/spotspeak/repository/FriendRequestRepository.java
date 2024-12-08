package com.example.spotspeak.repository;

import com.example.spotspeak.entity.FriendRequest;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.enumeration.EFriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    boolean existsBySenderAndReceiverAndStatus(User sender, User receiver, EFriendRequestStatus status);

    List<FriendRequest> findBySenderAndStatus(User sender, EFriendRequestStatus status);

    List<FriendRequest> findByReceiverAndStatus(User receiver, EFriendRequestStatus status);

    @Query("SELECT f FROM FriendRequest f WHERE f.sender = :user OR f.receiver = :user")
    List<FriendRequest> findAllByUser(@Param("user") User user);

    @Query("SELECT f FROM FriendRequest f WHERE (f.sender = :user OR f.receiver = :user) AND f.status = :status")
    List<FriendRequest> findAllAcceptedByUser(@Param("user") User user, @Param("status") EFriendRequestStatus status);

    @Query("SELECT COUNT(f) > 0 FROM FriendRequest f WHERE ((f.sender = :user1 AND f.receiver = :user2) OR (f.sender = :user2 AND f.receiver = :user1)) AND f.status = :status")
    boolean existsAcceptedByUsers(@Param("user1") User user1, @Param("user2") User user2, @Param("status") EFriendRequestStatus status);

    @Query("SELECT f FROM FriendRequest f WHERE ((f.sender = :user1 AND f.receiver = :user2) OR (f.sender = :user2 AND f.receiver = :user1)) AND f.status = :status")
    Optional<FriendRequest> findAcceptedByUsers(@Param("user1") User user1, @Param("user2") User user2, @Param("status") EFriendRequestStatus status);

}
