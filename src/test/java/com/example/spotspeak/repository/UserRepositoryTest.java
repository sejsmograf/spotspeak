package com.example.spotspeak.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;

import com.example.spotspeak.TestDataFactory;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.entity.ResourceListener;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.service.StorageService;

@DataJpaTest
public class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ResourceRepository resourceRepository;

	@MockBean
	private StorageService storageService;

	@InjectMocks
	private ResourceListener resourceListener;

	@Test
	@Rollback
	public void saveUser_withoutProfilePicture_shouldSucceed() {
		User user = TestDataFactory.createValidUser();
		User savedUser = userRepository.save(user);

		assertThat(savedUser).isNotNull();
		assertThat(savedUser.getProfilePicture()).isNull();
		assertThat(savedUser).isEqualTo(user);
	}

	@Test
	@Rollback
	public void saveUser_withProfilePicture_shouldSucceed() {
		User user = TestDataFactory.createValidUser();
		Resource profilePicture = Resource.builder()
				.resourceKey("test.jpg")
				.fileType("image/jpeg")
				.build();
		resourceRepository.save(profilePicture);
		user.setProfilePicture(profilePicture);

		User savedUser = userRepository.save(user);

		assertThat(savedUser).isNotNull();
		assertThat(savedUser).isEqualTo(user);
		assertThat(savedUser.getProfilePicture()).isNotNull();
		assertThat(savedUser.getProfilePicture()).isEqualTo(profilePicture);
	}

	@Test
	@Rollback
	public void savedUser_whenRetrievedById_shouldBeEqual() {
		User testUser = TestDataFactory.createValidUser();
		userRepository.save(testUser);

		User foundUser = userRepository.findById(testUser.getId()).orElse(null);

		assertThat(foundUser).isNotNull();
		assertThat(foundUser).isEqualTo(testUser);
	}

	@Test
	@Rollback
	public void saveedUserWithProfilePicture_whenDeleted_shouldDeleteProfilePicture() {
		User testUser = TestDataFactory.createValidUser();
		Resource profilePicture = resourceRepository.save(Resource.builder()
				.resourceKey("test.jpg")
				.fileType("image/jpeg")
				.build());
		testUser.setProfilePicture(profilePicture);

		User savedUser = userRepository.save(testUser);
		userRepository.deleteById(savedUser.getId());

		assertThat(userRepository.findById(savedUser.getId()).orElse(null)).isNull();
		assertThat(resourceRepository.findById(profilePicture.getId()).orElse(null)).isNull();
		assertTrue(resourceRepository.findAll().isEmpty());
	}
}
