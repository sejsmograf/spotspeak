package com.example.spotspeak.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.spotspeak.BaseTest;

import jakarta.persistence.EntityManager;

@SpringBootTest
public class BaseServiceIntegrationTest extends BaseTest {

    @Autowired
    protected EntityManager entityManager;

    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    protected <T> T persistAndGet(T entity) {
        entityManager.persist(entity);
        entityManager.flush();
        entityManager.clear();
        return entity;
    }
}