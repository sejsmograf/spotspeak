package com.example.spotspeak.repository;

import com.example.spotspeak.BaseTest;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BaseRepositoryTest extends BaseTest {
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
