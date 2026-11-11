package com.clinic.doctor_office.security;

import com.clinic.doctor_office.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Single place that turns security exceptions into JSON. It is reached from two sides:
 *  - exceptions thrown inside controllers (a failed login, or @PreAuthorize saying no),
 *    which Spring MVC routes here directly;
 *  - exceptions raised in the security filters (no/invalid token), which
 *    RestAuthenticationEntryPoint and RestAccessDeniedHandler forward here.
 * So 401/403 always have the same shape.
 *
 * Note for the future: a general "GlobalExceptionHandler" must NOT catch AccessDeniedException
 * or AuthenticationException (e.g. via a catch-all @ExceptionHandler(Exception.class)),
 * or it would turn 401/403 into 500.
 */
@RestControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        // Wrong password, unknown user and disabled account all get the SAME message,
        // so an attacker cannot learn which usernames exist.
        boolean loginFailure = ex instanceof BadCredentialsException
                || ex instanceof AccountStatusException
                || ex instanceof UsernameNotFoundException;
        String message = loginFailure
                ? "Invalid username or password"
                : "Authentication is required: send a valid Bearer token";

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
                .body(ApiError.of(HttpStatus.UNAUTHORIZED, message, request.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.of(HttpStatus.FORBIDDEN,
                        "You do not have permission to perform this action", request.getRequestURI()));
    }
}
