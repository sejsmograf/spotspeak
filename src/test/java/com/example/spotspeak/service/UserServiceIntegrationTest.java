package com.example.spotspeak.service;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.multipart.MultipartFile;

import com.example.spotspeak.exception.UserNotFoundException;
import com.example.spotspeak.dto.AuthenticatedUserProfileDTO;
import com.example.spotspeak.dto.PasswordUpdateDTO;
import com.example.spotspeak.dto.PublicUserProfileDTO;
import com.example.spotspeak.dto.UserUpdateDTO;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.repository.TestEntityFactory;

import jakarta.transaction.Transactional;

public class UserServiceIntegrationTest
        extends BaseServiceIntegrationTest {

    @MockBean
    private KeycloakClientService keycloakClientService;

    @Autowired
    private StorageService storageService;

    @Autowired
    @InjectMocks
    private UserService userService;

    private List<User> testUsers;
    private final int USER_COUNT = 5;

    @BeforeEach
    void setUp() {
        testUsers = new ArrayList<>();

        for (int i = 0; i < USER_COUNT; i++) {
            User createdUser = TestEntityFactory.createPersistedUser(entityManager);
            testUsers.add(createdUser);
        }
    }

    @AfterEach
    public void cleanStorage() {
        storageService.cleanUp();
    }

    @Test
    @Transactional
    void searchByUsername_shouldReturnUsersMatchingPartially() {
        testUsers.get(0).setUsername("findme");
        testUsers.get(1).setUsername("findmetoo");

        List<PublicUserProfileDTO> foundUsers = userService.searchUsersByUsername("find");

        assertThat(foundUsers).isNotEmpty().hasSizeGreaterThan(1);

    }

    @Test
    @Transactional
    void updatePassword_shouldPass() {
        String userId = testUsers.get(0).getId().toString();
        PasswordUpdateDTO dto = new PasswordUpdateDTO("current", "new");
        userService.updateUserPassword(userId, dto);
    }

    @Test
    @Transactional
    void getUserInfo_shouldReturnCorrectDto() {
        User user = testUsers.get(0);
        String userId = user.getId().toString();

        AuthenticatedUserProfileDTO dto = userService.getUserInfo(userId);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(user.getId());
    }

    @Test
    @Transactional
    void getUserInfo_shouldThrow_whenUserIdNonExistent() {
        UUID nonExistentUserId = UUID.randomUUID();

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserInfo(nonExistentUserId.toString()));
    }

    @Test
    @Transactional
    void getUserInfo_shouldThrow_whenInvalidIdFormat() {
        String invalidUserId = "invalid";

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserInfo(invalidUserId));
    }

    @Test
    @Transactional
    void updateUserProfilePicture_shouldUpdateUserAndUploadFile_whenUserHasNoProfilePicture() {
        User user = testUsers.get(0);
        String userId = user.getId().toString();
        MultipartFile mockFile = TestEntityFactory.createMockMultipartFile("image/png", 100);

        userService.updateUserProfilePicture(userId, mockFile);

        Resource userProfilePicture = user.getProfilePicture();
        Resource retrievedResource = entityManager.find(Resource.class, userProfilePicture.getId());
        boolean resourceExists = storageService.fileExists(userProfilePicture.getResourceKey());

        assertThat(userProfilePicture).isNotNull();
        assertThat(userProfilePicture.getFileType()).isEqualTo("image/png");
        assertThat(retrievedResource).isNotNull();
        assertThat(userProfilePicture.getId()).isEqualTo(retrievedResource.getId());
        assertThat(resourceExists).isTrue();
    }

    @Test
    @Transactional
    void updateUserProfilePicture_shouldUpdateUserAndPersistFile_whenUserHasProfilePicture() {
        User user = testUsers.get(0);
        String userId = user.getId().toString();
        MultipartFile mockFile = TestEntityFactory.createMockMultipartFile("image/png", 100);

        userService.updateUserProfilePicture(userId, mockFile);
        Resource firstProfilePicture = user.getProfilePicture();
        boolean firstResourceExists = storageService.fileExists(firstProfilePicture.getResourceKey());

        assertThat(firstProfilePicture).isNotNull();
        assertThat(firstProfilePicture.getFileType()).isEqualTo("image/png");
        assertThat(firstResourceExists).isTrue();

        MultipartFile mockFile2 = TestEntityFactory.createMockMultipartFile("image/jpg", 1000);
        userService.updateUserProfilePicture(userId, mockFile2);
        Resource secondProfilePicture = user.getProfilePicture();
        boolean secondResourceExists = storageService.fileExists(secondProfilePicture.getResourceKey());

        Resource retrievedFirstResource = entityManager.find(Resource.class, firstProfilePicture.getId());

        assertThat(retrievedFirstResource).isNull();
        assertThat(secondProfilePicture).isNotNull();
        assertThat(secondProfilePicture.getFileType()).isEqualTo("image/jpg");
        assertThat(secondResourceExists).isTrue();
    }

    @Test
    @Transactional
    public void updateUser_shouldUpdateUser_whenUserExists() {
        User user = testUsers.get(0);
        String userId = user.getId().toString();
        UserUpdateDTO updateDTO = new UserUpdateDTO("Firstname", null, null, "username");

        User updatedUser = userService.updateUser(userId, updateDTO);

        assertThat(updatedUser.getUsername()).isEqualTo(updateDTO.username());
        assertThat(updatedUser.getFirstName()).isEqualTo(updateDTO.firstName());
    }

    @Test
    @Transactional
    public void updateUser_shouldNotUpdateProperties_whenNull() {
        User user = testUsers.get(0);
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String email = user.getEmail();
        String username = user.getUsername();

        String userId = user.getId().toString();
        UserUpdateDTO updateDTO = new UserUpdateDTO(null, null, null, null);

        User updatedUser = userService.updateUser(userId, updateDTO);

        assertThat(updatedUser.getFirstName()).isNotNull().isEqualTo(firstName);
        assertThat(updatedUser.getLastName()).isNotNull().isEqualTo(lastName);
        assertThat(updatedUser.getEmail()).isNotNull().isEqualTo(email);
        assertThat(updatedUser.getUsername()).isNotNull().isEqualTo(username);
    }

    @Test
    @Transactional
    public void deledeById_shouldDeleteUser_withoutProfilePicture() {
        User user = testUsers.get(0);
        String userId = user.getId().toString();

        userService.deleteById(userId);

        User deletedUser = entityManager.find(User.class, user.getId());
        assertThat(deletedUser).isNull();
    }

    @Test
    @Transactional
    public void deledeById_shouldDeleteUserAndProfilePicture_whenUserHasProfilePicture() {
        User user = testUsers.get(0);
        String userId = user.getId().toString();
        MultipartFile mockFile = TestEntityFactory.createMockMultipartFile("image/png", 100);

        userService.updateUserProfilePicture(userId, mockFile);
        Resource userProfilePicture = user.getProfilePicture();
        boolean resourceExists = storageService.fileExists(userProfilePicture.getResourceKey());
        assertThat(resourceExists).isTrue();

        userService.deleteById(userId);

        User deletedUser = entityManager.find(User.class, user.getId());
        Resource deletedResource = entityManager.find(Resource.class, userProfilePicture.getId());
        boolean deletedResourceExists = storageService.fileExists(userProfilePicture.getResourceKey());

        assertThat(deletedUser).isNull();
        assertThat(deletedResource).isNull();
        assertThat(deletedResourceExists).isFalse();
    }
}
