package com.example.spotspeak.repository;

import com.example.spotspeak.BaseTestWithPostgres;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class BaseRepositoryTest extends BaseTestWithPostgres {
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
