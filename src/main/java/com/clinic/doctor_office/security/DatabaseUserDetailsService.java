package com.clinic.doctor_office.security;

import com.clinic.doctor_office.domain.User;
import com.clinic.doctor_office.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Tells Spring Security how to find a user by username: we read it from the database.
 * Spring's authentication machinery (DaoAuthenticationProvider) calls this at login,
 * then compares the submitted password with the stored BCrypt hash itself.
 */
@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // passwordHash and role are nullable columns in the entity; a user without them
        // can never log in (this avoids a NullPointerException later).
        if (user.getPasswordHash() == null || user.getRole() == null) {
            throw new UsernameNotFoundException("User is not fully configured");
        }

        return new AppUserDetails(
                user.getUsername(), user.getPasswordHash(), user.getRole(), user.isEnabled());
    }
}
