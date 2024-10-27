package com.example.spotspeak.repository;

import com.example.spotspeak.entity.Resource;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

}
