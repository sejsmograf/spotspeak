package com.example.spotspeak.service;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.example.spotspeak.dto.AuthenticatedUserProfileDTO;
import com.example.spotspeak.dto.PasswordUpdateDTO;
import com.example.spotspeak.dto.PublicUserProfileDTO;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.repository.TestEntityFactory;

import jakarta.transaction.Transactional;

public class UserServiceIntegrationTest
        extends BaseServiceIntegrationTest {

    @Autowired
    @InjectMocks
    private UserService userService;

    @MockBean
    private KeycloakClientService keycloakClientService;

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

}
