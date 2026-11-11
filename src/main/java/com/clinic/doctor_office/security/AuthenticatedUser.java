package com.clinic.doctor_office.security;

import com.clinic.doctor_office.domain.Role;

import java.security.Principal;

/**
 * Who is calling, as proven by a valid JWT. This is the principal stored in the
 * SecurityContext for every authenticated request, so controllers can ask for it with
 * {@code @AuthenticationPrincipal AuthenticatedUser user}.
 * Implementing Principal makes {@code authentication.getName()} return the username.
 */
public record AuthenticatedUser(String username, Role role) implements Principal {

    @Override
    public String getName() {
        return username;
    }
}
