package com.clinic.doctor_office.config;

import com.clinic.doctor_office.domain.Role;
import com.clinic.doctor_office.domain.User;
import com.clinic.doctor_office.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Mockito + ArgumentCaptor: we let the seeder run against a fake repository and then inspect
 * exactly what it tried to save.
 */
@ExtendWith(MockitoExtension.class)
class DevDataSeederTest {

    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void run_whenNoUsersExist_savesDoctorAndAssistantWithHashedPasswords() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        new DevDataSeeder(userRepository, passwordEncoder).run();

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(2)).save(saved.capture());
        List<User> users = saved.getAllValues();

        User doctor = users.stream().filter(u -> u.getRole() == Role.DOCTOR).findFirst().orElseThrow();
        User assistant = users.stream().filter(u -> u.getRole() == Role.ASSISTANT).findFirst().orElseThrow();

        assertThat(doctor.getUsername()).isEqualTo(DevDataSeeder.DOCTOR_USERNAME);
        assertThat(doctor.getPasswordHash()).isNotEqualTo(DevDataSeeder.DOCTOR_PASSWORD);
        assertThat(passwordEncoder.matches(DevDataSeeder.DOCTOR_PASSWORD, doctor.getPasswordHash())).isTrue();

        assertThat(assistant.getUsername()).isEqualTo(DevDataSeeder.ASSISTANT_USERNAME);
        assertThat(passwordEncoder.matches(DevDataSeeder.ASSISTANT_PASSWORD, assistant.getPasswordHash())).isTrue();
    }

    @Test
    void run_whenDoctorAlreadyExists_doesNotCreateItAgain() {
        User existing = User.builder().username(DevDataSeeder.DOCTOR_USERNAME).build();
        when(userRepository.findByUsername(DevDataSeeder.DOCTOR_USERNAME)).thenReturn(Optional.of(existing));
        when(userRepository.findByUsername(DevDataSeeder.ASSISTANT_USERNAME)).thenReturn(Optional.empty());

        new DevDataSeeder(userRepository, passwordEncoder).run();

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(saved.capture());
        assertThat(saved.getValue().getUsername()).isEqualTo(DevDataSeeder.ASSISTANT_USERNAME);
        verify(userRepository, never()).save(existing);
    }
}
