package com.clinic.doctor_office.service;

import com.clinic.doctor_office.dto.LoginResponse;
import com.clinic.doctor_office.security.AppUserDetails;
import com.clinic.doctor_office.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Login use case: check the credentials, then issue a token.
 * A wrong password, unknown user or disabled account makes authenticate() throw an
 * AuthenticationException; we let it propagate, and SecurityExceptionHandler turns it into a
 * generic 401.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponse login(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(username, password));

        AppUserDetails user = (AppUserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(user.getUsername(), user.getRole());
        return new LoginResponse(token, "Bearer", jwtService.getExpiresInSeconds());
    }
}
