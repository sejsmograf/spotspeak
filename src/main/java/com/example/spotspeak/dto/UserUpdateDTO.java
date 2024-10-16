package com.example.spotspeak.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdateDTO {
    private String firstName;
    private String lastName;
}
