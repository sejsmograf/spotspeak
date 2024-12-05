package com.example.spotspeak.repository;

import com.example.spotspeak.entity.FriendRequest;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.enumeration.EFriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    boolean existsBySenderAndReceiverAndStatus(User sender, User receiver, EFriendRequestStatus status);

    List<FriendRequest> findBySenderAndStatus(User sender, EFriendRequestStatus status);

    List<FriendRequest> findByReceiverAndStatus(User receiver, EFriendRequestStatus status);

    @Query("SELECT f FROM FriendRequest f WHERE f.sender = :user OR f.receiver = :user")
    List<FriendRequest> findAllByUser(@Param("user") User user);
}
