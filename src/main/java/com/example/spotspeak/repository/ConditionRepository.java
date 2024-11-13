package com.example.spotspeak.repository;

import com.example.spotspeak.entity.achievement.Condition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConditionRepository extends JpaRepository<Condition, Long> {

}