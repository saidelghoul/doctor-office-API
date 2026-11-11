package com.clinic.doctor_office.security;

import com.clinic.doctor_office.domain.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Plain unit test: no Spring, no mocks. JwtService has no hidden dependencies, so we just
 * build it with new. A fixed Clock lets us "travel in time" to test expiry instantly.
 */
class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-unit-test-secret-1234";
    private static final Instant NOW = Instant.parse("2026-01-15T10:00:00Z");

    private final JwtProperties properties = new JwtProperties(SECRET, 15);
    private final JwtService jwtService = serviceAt(NOW);

    private JwtService serviceAt(Instant instant) {
        return new JwtService(properties, Clock.fixed(instant, ZoneOffset.UTC));
    }

    @Test
    void generatedToken_canBeParsedBack_withUsernameAndRole() {
        String token = jwtService.generateToken("dr.house", Role.DOCTOR);

        Optional<AuthenticatedUser> parsed = jwtService.parse(token);

        assertThat(parsed).contains(new AuthenticatedUser("dr.house", Role.DOCTOR));
    }

    @Test
    void token_isStillValidJustBeforeExpiry() {
        String token = jwtService.generateToken("dr.house", Role.DOCTOR);

        JwtService fourteenMinutesLater = serviceAt(NOW.plus(14, ChronoUnit.MINUTES));

        assertThat(fourteenMinutesLater.parse(token)).isPresent();
    }

    @Test
    void token_isRejectedAfterExpiry() {
        String token = jwtService.generateToken("dr.house", Role.DOCTOR);

        JwtService sixteenMinutesLater = serviceAt(NOW.plus(16, ChronoUnit.MINUTES));

        assertThat(sixteenMinutesLater.parse(token)).isEmpty();
    }

    @Test
    void token_signedWithAnotherSecret_isRejected() {
        JwtService attacker = new JwtService(
                new JwtProperties("another-secret-another-secret-another-1", 15),
                Clock.fixed(NOW, ZoneOffset.UTC));
        String forged = attacker.generateToken("dr.house", Role.DOCTOR);

        assertThat(jwtService.parse(forged)).isEmpty();
    }

    @Test
    void token_withModifiedPayload_isRejected() {
        // A logged-in assistant edits the payload to become a doctor, keeping the old signature.
        String token = jwtService.generateToken("nurse.joy", Role.ASSISTANT);
        String[] parts = token.split("\\.");
        String forgedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"sub\":\"nurse.joy\",\"role\":\"DOCTOR\"}".getBytes(StandardCharsets.UTF_8));

        String tampered = parts[0] + "." + forgedPayload + "." + parts[2];

        assertThat(jwtService.parse(tampered)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "not-a-jwt", "a.b.c"})
    void garbage_isRejected_withoutThrowing(String garbage) {
        assertThat(jwtService.parse(garbage)).isEmpty();
    }

    @Test
    void expiresInSeconds_matchesConfiguredMinutes() {
        assertThat(jwtService.getExpiresInSeconds()).isEqualTo(15 * 60);
    }

    @Test
    void secretShorterThan32Bytes_isRefusedAtStartup() {
        JwtProperties weak = new JwtProperties("too-short", 15);

        assertThatThrownBy(() -> new JwtService(weak, Clock.systemUTC()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32");
    }
}
