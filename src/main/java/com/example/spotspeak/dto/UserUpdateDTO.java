package com.example.spotspeak.dto;

public record UserUpdateDTO(
        String firstName,
        String lastName,
        String email,
        String username) {

}
