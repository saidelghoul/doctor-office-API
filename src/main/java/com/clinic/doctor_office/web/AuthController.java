package com.clinic.doctor_office.web;

import com.clinic.doctor_office.dto.CurrentUserResponse;
import com.clinic.doctor_office.dto.LoginRequest;
import com.clinic.doctor_office.dto.LoginResponse;
import com.clinic.doctor_office.security.AppUserDetails;
import com.clinic.doctor_office.security.AuthenticatedUser;
import com.clinic.doctor_office.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Checks the credentials and returns a JWT. A wrong password, unknown user or disabled
     * account makes authenticate() throw an AuthenticationException, which
     * SecurityExceptionHandler turns into a generic 401.
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));

        AppUserDetails user = (AppUserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(user.getUsername(), user.getRole());
        return new LoginResponse(token, "Bearer", jwtService.getExpiresInSeconds());
    }

    /** Who am I? Needs a valid token; handy for the Angular frontend later. */
    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
        return new CurrentUserResponse(user.username(), user.role());
    }
}
