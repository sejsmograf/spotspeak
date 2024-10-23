package com.example.spotspeak.dto;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ErrorResponse {

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
        private ZonedDateTime timestamp;

        private Integer statusCode;

        private List<String> message;

        public static ErrorResponse createInstance() {
                return ErrorResponse.builder()
                                .timestamp(ZonedDateTime.now())
                                .build();
        }

        public ErrorResponse addMessage(String message) {
                if (this.message == null) {
                        this.message = new ArrayList<>();
                }
                this.message.add(message);
                return this;
        }
}
