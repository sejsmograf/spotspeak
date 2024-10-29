package com.example.spotspeak.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.spotspeak.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u " +
            "FROM User u " +
            "WHERE u.username LIKE  %:username%")
    List<User> findAllByUsernameIgnoreCase(String username);
}
