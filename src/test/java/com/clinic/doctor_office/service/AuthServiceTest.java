package com.clinic.doctor_office.service;

import com.clinic.doctor_office.domain.Role;
import com.clinic.doctor_office.dto.LoginResponse;
import com.clinic.doctor_office.security.AppUserDetails;
import com.clinic.doctor_office.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Mockito unit test: AuthenticationManager and JwtService are mocked, so only AuthService's logic runs. */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_withValidCredentials_returnsTokenForTheAuthenticatedUser() {
        AppUserDetails principal = new AppUserDetails("dr.house", "hash", Role.DOCTOR, true);
        Authentication authenticated =
                UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authenticated);
        when(jwtService.generateToken("dr.house", Role.DOCTOR)).thenReturn("the.jwt.token");
        when(jwtService.getExpiresInSeconds()).thenReturn(900L);

        LoginResponse response = authService.login("dr.house", "secret");

        assertThat(response.accessToken()).isEqualTo("the.jwt.token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(900L);
    }

    @Test
    void login_withBadCredentials_propagatesExceptionAndIssuesNoToken() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login("dr.house", "wrong"))
                .isInstanceOf(BadCredentialsException.class);

        verifyNoInteractions(jwtService);
    }
}
