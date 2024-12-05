package com.example.spotspeak.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.MockMvc;

import com.example.spotspeak.BaseTestWithKeycloak;

import jakarta.transaction.Transactional;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class UserControllerTest
        extends BaseTestWithKeycloak {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserProfileController profileController;

}
