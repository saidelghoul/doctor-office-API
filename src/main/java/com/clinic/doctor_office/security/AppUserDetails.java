package com.clinic.doctor_office.security;

import com.clinic.doctor_office.domain.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adapter between our domain User and what Spring Security understands (UserDetails).
 * It is only used while logging in (DaoAuthenticationProvider reads the password hash and
 * the enabled flag from it). It is NOT the principal of a request authenticated by a JWT:
 * that one is an {@link AuthenticatedUser}.
 */
public class AppUserDetails implements UserDetails {

    private final String username;
    private final String passwordHash;
    private final Role role;
    private final boolean enabled;

    public AppUserDetails(String username, String passwordHash, Role role, boolean enabled) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.enabled = enabled;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring's hasRole('DOCTOR') looks for the authority "ROLE_DOCTOR".
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
