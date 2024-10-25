package com.example.spotspeak.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;

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

	private User testUser;

	@BeforeEach
	public void setUp() {
		testUser = User.builder()
				.id(UUID.randomUUID())
				.username("testuser")
				.firstName("Test")
				.lastName("User")
				.profilePicture(null)
				.build();
	}

	@Test
	@Rollback
	public void shouldSaveUserWithoutProfilePictureSuccessfully() {
		User savedUser = userRepository.save(testUser);

		assertThat(savedUser).isNotNull();
		assertThat(savedUser.getProfilePicture()).isNull();
		assertThat(savedUser).isEqualTo(testUser);
	}

	@Test
	@Rollback
	public void shouldSaveUserWithProfilePictureSuccessfully() {
		Resource profilePicture = Resource.builder()
				.resourceKey("test.jpg")
				.fileType("image/jpeg")
				.build();
		resourceRepository.save(profilePicture);
		testUser.setProfilePicture(profilePicture);

		User savedUser = userRepository.save(testUser);

		assertThat(savedUser).isNotNull();
		assertThat(savedUser).isEqualTo(testUser);
		assertThat(savedUser.getProfilePicture()).isNotNull();
		assertThat(savedUser.getProfilePicture()).isEqualTo(profilePicture);
	}

	@Test
	@Rollback
	public void shouldFindUserByIdSuccessfully() {
		UUID id = testUser.getId();
		userRepository.save(testUser);

		User foundUser = userRepository.findById(id).orElse(null);

		assertThat(foundUser).isNotNull();
		assertThat(foundUser).isEqualTo(testUser);
	}

	@Test
	@Rollback
	public void shouldDeleteUserProfilePicture() {
		UUID id = testUser.getId();
		userRepository.save(testUser);

		User foundUser = userRepository.findById(id).orElse(null);

		assertThat(foundUser).isNotNull();
		assertThat(foundUser).isEqualTo(testUser);
	}

	@Test
	@Rollback
	public void shouldCascadeDeleteProfilePictureWhenUserIsDeleted() {
		Resource profilePicture = resourceRepository.save(Resource.builder()
				.resourceKey("test.jpg")
				.fileType("image/jpeg")
				.build());
		testUser.setProfilePicture(profilePicture);

		User savedUser = userRepository.save(testUser);
		userRepository.deleteById(savedUser.getId());

		assertThat(userRepository.findById(savedUser.getId()).orElse(null)).isNull();
		assertThat(resourceRepository.findById(profilePicture.getId()).orElse(null)).isNull();
	}
}
