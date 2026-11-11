package com.clinic.doctor_office.security;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Typed view of the app.jwt.* properties (binds app.jwt.secret and app.jwt.expiration-minutes).
 * Registered in ApplicationConfig. If a value is missing or invalid the application
 * refuses to start, instead of failing later at the first login.
 */
@ConfigurationProperties(prefix = "app.jwt")
@Validated
public record JwtProperties(@NotBlank String secret, @Min(1) long expirationMinutes) {
}
