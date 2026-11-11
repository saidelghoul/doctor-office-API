package com.clinic.doctor_office.config;

import com.clinic.doctor_office.security.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Small application-wide beans.
 *  - Clock: Spring Boot does not provide one. Injecting it (instead of calling Instant.now()
 *    everywhere) lets tests control time. Appointments will need it too.
 *  - Registers JwtProperties so app.jwt.* is bound and validated at startup.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class ApplicationConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
