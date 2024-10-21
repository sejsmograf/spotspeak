package com.example.spotspeak.repository;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import com.example.spotspeak.entity.User;

public interface UserRepository extends CrudRepository<User, UUID> {

}
