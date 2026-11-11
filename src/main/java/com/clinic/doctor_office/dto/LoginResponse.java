package com.clinic.doctor_office.dto;

/** Returned by POST /api/auth/login. The client sends "Authorization: Bearer <accessToken>". */
public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
}
