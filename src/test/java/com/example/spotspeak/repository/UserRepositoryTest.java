package com.example.spotspeak.repository;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.example.spotspeak.TestEntityFactory;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.entity.ResourceListener;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.service.StorageService;

public class UserRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @MockBean
    private StorageService storageService;

    @InjectMocks
    private ResourceListener resourceListener;

    @Nested
    class BasicUserOperationsTests {
        @Test
        void saveUser_shouldPersist_whenNoProfilePicture() {
            User user = TestEntityFactory.createPersistedUser(entityManager);

            assertThat(user.getId()).isNotNull();
        }

        @Test
        void findUserById_shouldReturnCorrectUser() {
            User user = TestEntityFactory.createPersistedUser(entityManager);
            flushAndClear();

            User found = userRepository.findById(user.getId()).orElseThrow();

            assertThat(found).isEqualTo(user);
        }
    }

    @Nested
    class UserProfilePicutreTests {
        @Test
        void saveUser_shouldPersist_whenProfilePictureProvided() {
            User user = TestEntityFactory.createPersistedUser(entityManager);
            Resource profilePicture = TestEntityFactory.createPersistedResource(entityManager);
            user.setProfilePicture(profilePicture);

            flushAndClear();
            User found = userRepository.findById(user.getId()).orElseThrow();

            assertThat(found.getProfilePicture()).isNotNull().isEqualTo(profilePicture);
        }

        @Test
        void deleteUser_shouldNotDeleteProfilePicture() {
            User user = TestEntityFactory.createPersistedUser(entityManager);
            Resource profilePicture = TestEntityFactory.createPersistedResource(entityManager);
            user.setProfilePicture(profilePicture);

            flushAndClear();

            userRepository.deleteById(user.getId());
            flushAndClear();

            assertThat(userRepository.findById(user.getId())).isNotPresent();
            assertThat(resourceRepository.findById(profilePicture.getId())).isNotPresent();
        }
    }

    @Nested
    class UserSearchTest {
        @Test
        void findByUsername_shouldContainUser_whenUsernameTheSame() {
            User user = TestEntityFactory.createPersistedUser(entityManager);
            String username = "findme123";
            user.setUsername(username);
            flushAndClear();

            List<User> found = userRepository.findAllByUsernameIgnoreCase(username);

            assertThat(found).isNotEmpty().containsExactly(user);
        }

        @Test
        void findByUsername_shouldReturnEmpty_whenUsernameDiffers() {
            User user = TestEntityFactory.createPersistedUser(entityManager);
            String username = "findme123";
            String searchUsername = "dontfindme";
            user.setUsername(username);
            flushAndClear();

            List<User> found = userRepository.findAllByUsernameIgnoreCase(searchUsername);

            assertThat(found).isEmpty();
        }

        @Test
        void findByUsername_shouldContainUser_whenSearchedByUsernameBeginning() {
            User user = TestEntityFactory.createPersistedUser(entityManager);
            String username = "findme123";
            user.setUsername(username);

            String partialUsername = username.substring(0, 3);
            List<User> found = userRepository.findAllByUsernameIgnoreCase(partialUsername);

            assertThat(found).isNotEmpty().containsExactly(user);
        }

        @Test
        void findByUsername_shouldContainAllMatching_whenMultipleUsersMatch() {
            String baseUsername = "findme";
            for (int i = 0; i < 5; i++) {
                User user = TestEntityFactory.createPersistedUser(entityManager);
                String username = baseUsername.concat(String.valueOf(i));
                user.setUsername(username);
            }

            List<User> found = userRepository.findAllByUsernameIgnoreCase(baseUsername);

            assertThat(found).hasSize(5).containsAll(userRepository.findAll());
        }
    }
}
