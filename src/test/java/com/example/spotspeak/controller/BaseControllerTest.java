package com.example.spotspeak.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.example.spotspeak.BaseTestWithKeycloak;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class BaseControllerTest extends
        BaseTestWithKeycloak {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
}
