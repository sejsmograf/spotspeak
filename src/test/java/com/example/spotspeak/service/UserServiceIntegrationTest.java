package com.example.spotspeak.service;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.example.spotspeak.exception.AttributeAlreadyExistsException;
import com.example.spotspeak.exception.PasswordChallengeFailedException;
import com.example.spotspeak.exception.UserNotFoundException;
import com.example.spotspeak.dto.AuthenticatedUserProfileDTO;
import com.example.spotspeak.dto.PasswordUpdateDTO;
import com.example.spotspeak.dto.PublicUserWithFriendshipDTO;
import com.example.spotspeak.dto.RegisteredUserDTO;
import com.example.spotspeak.dto.UserUpdateDTO;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.achievement.Achievement;
import com.example.spotspeak.entity.enumeration.EEventType;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.BaseTestWithKeycloak;
import com.example.spotspeak.TestEntityFactory;

import jakarta.transaction.Transactional;

public class UserServiceIntegrationTest
        extends BaseTestWithKeycloak {

    @Autowired
    private StorageService storageService;

    @Autowired
    private UserService userService;

    @Autowired
    private KeycloakClientService keycloakClientService;

    private List<User> testUsers;

    @BeforeEach
    void setUp() {
        List<UserRepresentation> keycloakUsers = getKeycloakUsers();

        testUsers = new ArrayList<>();
        for (UserRepresentation keycloakUser : keycloakUsers) {
            User persistedUser = TestEntityFactory.createdPersistedUser(entityManager, keycloakUser);
            testUsers.add(persistedUser);
        }
    }

    @AfterEach
    public void cleanStorage() {
        storageService.cleanUp();
    }

    @Test
    @Transactional
    void searchByUsername_shouldReturnUsersMatchingPartially() {
        User user = testUsers.get(0);

        String usernameToSearch = testUsers.get(0).getUsername().substring(0, 2);
        int expectedSize = testUsers.stream()
                .filter(u -> u.getUsername().startsWith(usernameToSearch) && !u.getId().equals(user.getId()))
                .toList().size();

        List<PublicUserWithFriendshipDTO> foundUsers = userService.searchUsersByUsername(user.getId().toString(),
                usernameToSearch);
        assertThat(foundUsers).isNotEmpty().hasSize(expectedSize);
    }

    @Test
    @Transactional
    void updatePassword_shouldThrow_whenCurrentPasswordDoesntMatch() {
        String userId = testUsers.get(0).getId().toString();
        PasswordUpdateDTO dto = new PasswordUpdateDTO("wrong", "new");

        assertThrows(PasswordChallengeFailedException.class,
                () -> userService.updateUserPassword(userId, dto));
    }

    @Test
    @Transactional
    void updatePassword_shouldWork_whenCurrentPasswordMatches() {
        User user = testUsers.get(0);
        String username = user.getUsername();
        String oldPassword = username; // testusers have the same username and password
        String userId = user.getId().toString();
        String newPassword = "newpassword1";
        PasswordUpdateDTO dto = new PasswordUpdateDTO(oldPassword, newPassword);

        userService.updateUserPassword(userId, dto);

        keycloakClientService.validatePasswordOrThrow(userId, newPassword);

        // cleanup - reset password to original one
        PasswordUpdateDTO resetDto = new PasswordUpdateDTO(newPassword, oldPassword);
        userService.updateUserPassword(userId, resetDto);
        keycloakClientService.validatePasswordOrThrow(userId, oldPassword);
    }

    @Test
    @Transactional
    void generateTemporaryToken_shouldReturnToken_whenPasswordMatches() {
        User user = testUsers.get(0);
        String userId = user.getId().toString();
        String password = user.getUsername(); // testusers have the same username and password

        String token = userService.generateTemporaryToken(userId, password).token();
        assertThat(token).isNotBlank();
    }

    @Test
    @Transactional
    void generateTemporaryToken_shouldThrowWhenPasswordDoesntMatch() {
        User user = testUsers.get(0);
        String userId = user.getId().toString();
        String password = user.getUsername() + "notcorrect";

        assertThrows(PasswordChallengeFailedException.class,
                () -> userService.generateTemporaryToken(userId, password).token());
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
    void getUserInfo_shouldReturnCorrectDto_whenUserHasProfilePicture() {
        User user = testUsers.get(0);
        String userId = user.getId().toString();
        MultipartFile mockFile = TestEntityFactory.createMockMultipartFile("image/png", 100);
        userService.updateUserProfilePicture(userId, mockFile);

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

        MultipartFile mockFile2 = TestEntityFactory.createMockMultipartFile("image/jpg", 1000);
        userService.updateUserProfilePicture(userId, mockFile2);

        Resource retrievedFirstResource = entityManager.find(Resource.class, firstProfilePicture.getId());
        Resource secondProfilePicture = user.getProfilePicture();
        boolean secondResourceExists = storageService.fileExists(secondProfilePicture.getResourceKey());
        assertThat(retrievedFirstResource).isNull();
        assertThat(secondProfilePicture).isNotNull();
        assertThat(secondProfilePicture.getFileType()).isEqualTo("image/jpg");
        assertThat(secondResourceExists).isTrue();
    }

    @Test
    @Transactional
    void updateUser_shouldThrow_whenInvalidPasswordChallengeToken() {
        User user = testUsers.get(0);
        String userId = user.getId().toString();

        UserUpdateDTO updateDTO = new UserUpdateDTO("notvalidtoken", "Firstname", "Lastname",
                "ornnit@olog.com", "username");

        assertThrows(PasswordChallengeFailedException.class, () -> userService.updateUser(userId, updateDTO));
    }

    @Test
    @Transactional
    void updateUser_shouldWork_whenValidPasswordChallengeToken() {
        User user = testUsers.get(0);
        String userId = user.getId().toString();
        String username = user.getUsername();
        String password = username;
        String challengeToken = userService.generateTemporaryToken(userId, password).token();
        String newFirstName = "newname";
        String newLastName = "newlastname";
        UserUpdateDTO updateDTO = UserUpdateDTO.builder()
                .firstName(newFirstName)
                .lastName(newLastName)
                .passwordChallengeToken(challengeToken)
                .build();

        userService.updateUser(userId, updateDTO);
        User retrievedUser = entityManager.find(User.class, user.getId());

        assertThat(retrievedUser.getFirstName()).isEqualTo(newFirstName);
        assertThat(retrievedUser.getLastName()).isEqualTo(newLastName);
    }

    @Test
    @Transactional
    void updateUser_shouldThrow_whenUsernameNotUnique() {
        User user = testUsers.get(0);
        String userId = user.getId().toString();
        String username = user.getUsername();
        String password = username;
        String challengeToken = userService.generateTemporaryToken(userId, password).token();
        User otherUser = testUsers.get(1);
        String otherUsername = otherUser.getUsername();

        UserUpdateDTO updateDTO = UserUpdateDTO.builder()
                .username(otherUsername)
                .passwordChallengeToken(challengeToken)
                .build();

        assertThrows(AttributeAlreadyExistsException.class,
                () -> userService.updateUser(userId, updateDTO));
    }

    @Test
    @Transactional
    void updateUser_shouldThrow_whenEmailNotUnique() {
        User user = testUsers.get(0);
        String userId = user.getId().toString();
        String username = user.getUsername();
        String password = username; // testusers have the same username and password
        String challengeToken = userService.generateTemporaryToken(userId, password).token();
        User otherUser = testUsers.get(1);
        String otherEmail = otherUser.getEmail();

        UserUpdateDTO updateDTO = UserUpdateDTO.builder()
                .passwordChallengeToken(challengeToken)
                .email(otherEmail)
                .build();

        assertThrows(AttributeAlreadyExistsException.class,
                () -> userService.updateUser(userId, updateDTO));
    }

    @Test
    @Transactional
    void updateUser_shouldNotUpdateProperties_whenNull() {
        User user = testUsers.get(0);
        String userId = user.getId().toString();
        String password = user.getUsername();
        String challengeToken = userService.generateTemporaryToken(userId, password).token();
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String email = user.getEmail();
        String username = user.getUsername();
        UserUpdateDTO updateDTO = new UserUpdateDTO(challengeToken, null, null, null,
                null);

        User updatedUser = userService.updateUser(userId, updateDTO);

        assertThat(updatedUser.getFirstName()).isNotNull().isEqualTo(firstName);
        assertThat(updatedUser.getLastName()).isNotNull().isEqualTo(lastName);
        assertThat(updatedUser.getEmail()).isNotNull().isEqualTo(email);
        assertThat(updatedUser.getUsername()).isNotNull().isEqualTo(username);
    }

    @Test
    @Transactional
    void deleteById_shouldDeleteUser_withoutProfilePicture() {
        User user = testUsers.get(0);
        String userId = user.getId().toString();

        userService.deleteById(userId);

        User deletedUser = entityManager.find(User.class, user.getId());
        assertThat(deletedUser).isNull();
        getKeycloakUsers().stream().map(UserRepresentation::getId)
                .forEach(id -> assertThat(id).isNotEqualTo(userId));
    }

    @Test
    @Transactional
    void deleteById_shouldDeleteUserAndProfilePicture_whenUserHasProfilePicture() {
        User user = testUsers.get(0);
        String userId = user.getId().toString();
        MultipartFile mockFile = TestEntityFactory.createMockMultipartFile("image/png", 100);
        userService.updateUserProfilePicture(userId, mockFile);
        Resource userProfilePicture = user.getProfilePicture();

        userService.deleteById(userId);

        User deletedUser = entityManager.find(User.class, user.getId());
        Resource deletedResource = entityManager.find(Resource.class,
                userProfilePicture.getId());
        boolean deletedResourceExists = storageService.fileExists(userProfilePicture.getResourceKey());
        assertThat(deletedUser).isNull();
        assertThat(deletedResource).isNull();
        assertThat(deletedResourceExists).isFalse();
    }

    @Test
    @Transactional
    void initializeUser_shouldThrow_whenUserAlreadyExists() {
        User user = testUsers.get(0);
        UUID userId = user.getId();
        String username = user.getUsername();
        String email = user.getEmail();
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        LocalDateTime registeredAt = user.getRegisteredAt();

        RegisteredUserDTO userDTO = new RegisteredUserDTO(userId, firstName, lastName, email, username, registeredAt);

        assertThrows(IllegalArgumentException.class, () -> userService.initializeUser(userDTO));
    }

    @Test
    @Transactional
    void initializeUser_shouldSaveUser_whenValidUser() {
        User newUser = User.builder()
                .id(UUID.randomUUID())
                .firstName("new")
                .lastName("user")
                .email("newuser@test.com")
                .username("newuser")
                .registeredAt(LocalDateTime.now())
                .build();
        RegisteredUserDTO userDTO = new RegisteredUserDTO(newUser.getId(), newUser.getFirstName(),
                newUser.getLastName(),
                newUser.getEmail(), newUser.getUsername(), newUser.getRegisteredAt());
        userService.initializeUser(userDTO);
        flushAndClear();

        User retrievedUser = entityManager.find(User.class, newUser.getId());
        assertThat(retrievedUser).isNotNull();
        assertThat(retrievedUser.getId()).isEqualTo(newUser.getId());
    }

    @Test
    @Transactional
    void initializeUser_shouldSaveUser_andInitializeAchievements() {
        User newUser = User.builder()
                .id(UUID.randomUUID())
                .firstName("new")
                .lastName("user")
                .email("newuser@test.com")
                .username("newuser")
                .registeredAt(LocalDateTime.now())
                .build();

        Achievement achievement = TestEntityFactory.createPersistedAchievement(
                entityManager,
                "Dodaj pierwszy ślad",
                "Dodaj pierwszy ślad",
                20,
                EEventType.ADD_TRACE,
                1,
                null);

        RegisteredUserDTO userDTO = new RegisteredUserDTO(newUser.getId(), newUser.getFirstName(),
                newUser.getLastName(),
                newUser.getEmail(), newUser.getUsername(), newUser.getRegisteredAt());
        userService.initializeUser(userDTO);
        flushAndClear();

        User retrievedUser = entityManager.find(User.class, newUser.getId());
        assertThat(retrievedUser.getUserAchievements()).isNotEmpty();
        assertThat(retrievedUser.getUserAchievements().get(0).getAchievement().getId()).isEqualTo(achievement.getId());
    }
}
