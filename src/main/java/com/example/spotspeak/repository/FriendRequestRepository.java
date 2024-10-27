package com.example.spotspeak.repository;

import com.example.spotspeak.entity.FriendRequest;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.enumeration.EFriendRequestStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FriendRequestRepository extends CrudRepository<FriendRequest, Long> {

    boolean existsBySenderAndReceiverAndStatus(User sender, User receiver, EFriendRequestStatus status);

    List<FriendRequest> findBySenderAndStatus(User sender, EFriendRequestStatus status);

    List<FriendRequest> findByReceiverAndStatus(User receiver, EFriendRequestStatus status);
}
