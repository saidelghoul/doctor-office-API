package com.clinic.doctor_office.security;

import com.clinic.doctor_office.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Creates and validates JWTs (HS256). No Spring Security types here, so it is easy to
 * unit test. Time comes from an injected java.time.Clock, so tests can fix "now" and
 * check expiry without sleeping.
 *
 * Token content: subject = username, one custom claim "role", issued-at and expiry.
 */
@Service
public class JwtService {

    private static final String ROLE_CLAIM = "role";
    private static final int MIN_SECRET_BYTES = 32; // HS256 needs a 256-bit key

    private final SecretKey key;
    private final Duration expiration;
    private final Clock clock;
    private final JwtParser parser;

    public JwtService(JwtProperties properties, Clock clock) {
        byte[] secretBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "app.jwt.secret must be at least " + MIN_SECRET_BYTES + " bytes long");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.expiration = Duration.ofMinutes(properties.expirationMinutes());
        this.clock = clock;
        // The parser checks signature AND expiry. We give it our Clock so "now" is the same
        // one used to create tokens. (io.jsonwebtoken.Clock is a one-method interface.)
        this.parser = Jwts.parser()
                .verifyWith(key)
                .clock(() -> Date.from(clock.instant()))
                .build();
    }

    public String generateToken(String username, Role role) {
        Instant now = clock.instant();
        return Jwts.builder()
                .subject(username)
                .claim(ROLE_CLAIM, role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Returns the user if the token is well formed, correctly signed, not expired and
     * carries a known role. Returns empty for ANY problem, so callers never deal with
     * jjwt exceptions (and never learn why a token was rejected).
     */
    public Optional<AuthenticatedUser> parse(String token) {
        try {
            Claims claims = parser.parseSignedClaims(token).getPayload();
            String username = claims.getSubject();
            String roleName = claims.get(ROLE_CLAIM, String.class);
            if (username == null || roleName == null) {
                return Optional.empty();
            }
            return Optional.of(new AuthenticatedUser(username, Role.valueOf(roleName)));
        } catch (JwtException | IllegalArgumentException e) {
            // JwtException: malformed, bad signature, expired...
            // IllegalArgumentException: empty token, or a role name that does not exist.
            return Optional.empty();
        }
    }

    public long getExpiresInSeconds() {
        return expiration.toSeconds();
    }
}
