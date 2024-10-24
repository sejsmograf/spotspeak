package com.example.spotspeak.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.spotspeak.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

}
