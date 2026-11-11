package com.clinic.doctor_office.security;

import com.clinic.doctor_office.domain.Role;
import com.clinic.doctor_office.domain.User;
import com.clinic.doctor_office.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Mockito unit test. The real UserRepository would need a database, so we replace it with a
 * mock (@Mock) that we program with when(...).thenReturn(...), and inject it into the class
 * under test (@InjectMocks). verify(...) checks the interaction we care about.
 */
@ExtendWith(MockitoExtension.class)
class DatabaseUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DatabaseUserDetailsService service;

    @Test
    void loadUserByUsername_whenUserExists_mapsHashRoleAndEnabledFlag() {
        User doctor = User.builder().username("dr.house").passwordHash("$2a$hash").role(Role.DOCTOR).enabled(true).build();
        when(userRepository.findByUsername("dr.house")).thenReturn(Optional.of(doctor));

        UserDetails details = service.loadUserByUsername("dr.house");

        assertThat(details.getUsername()).isEqualTo("dr.house");
        assertThat(details.getPassword()).isEqualTo("$2a$hash");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_DOCTOR");
        verify(userRepository).findByUsername("dr.house");
    }

    @Test
    void loadUserByUsername_whenUserIsDisabled_reportsDisabled() {
        User disabled = User.builder().username("old.assistant").passwordHash("hash").role(Role.ASSISTANT).enabled(false).build();
        when(userRepository.findByUsername("old.assistant")).thenReturn(Optional.of(disabled));

        assertThat(service.loadUserByUsername("old.assistant").isEnabled()).isFalse();
    }

    @Test
    void loadUserByUsername_whenUserDoesNotExist_throwsUsernameNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadUserByUsername_whenUserHasNoRole_throwsUsernameNotFound() {
        User incomplete = User.builder().username("broken").passwordHash("hash").role(null).build();
        when(userRepository.findByUsername("broken")).thenReturn(Optional.of(incomplete));

        assertThatThrownBy(() -> service.loadUserByUsername("broken"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
