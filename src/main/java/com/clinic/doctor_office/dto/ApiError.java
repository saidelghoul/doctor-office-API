package com.clinic.doctor_office.dto;

import org.springframework.http.HttpStatus;

import java.time.Instant;

/** JSON body returned for every error produced by the security layer (401, 403). */
public record ApiError(Instant timestamp, int status, String error, String message, String path) {

    public static ApiError of(HttpStatus status, String message, String path) {
        return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, path);
    }
}
