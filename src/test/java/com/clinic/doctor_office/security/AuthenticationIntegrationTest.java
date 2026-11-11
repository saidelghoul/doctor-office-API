package com.clinic.doctor_office.security;

import com.clinic.doctor_office.domain.Role;
import com.clinic.doctor_office.domain.User;
import com.clinic.doctor_office.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test: the REAL application context (security chain, filter, H2 database, BCrypt),
 * called through MockMvc, which simulates HTTP requests without starting a server.
 *
 * Each test starts from a clean users table and its own users, so it does not depend on the
 * dev seed data.
 *
 * MockMvc is built by hand with springSecurity() so the security filter chain is part of every
 * request. (@AutoConfigureMockMvc does the same; its import package changed in Spring Boot 4,
 * so this version avoids depending on it.)
 */
@SpringBootTest
class AuthenticationIntegrationTest {

    private static final String DOCTOR = "dr.test";
    private static final String ASSISTANT = "assistant.test";
    private static final String DISABLED = "disabled.test";

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private JwtProperties jwtProperties;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

        userRepository.deleteAll();
        saveUser(DOCTOR, "doctor-pass", Role.DOCTOR, true);
        saveUser(ASSISTANT, "assistant-pass", Role.ASSISTANT, true);
        saveUser(DISABLED, "disabled-pass", Role.ASSISTANT, false);
    }

    // ---------------------------------------------------------------- login

    @Test
    void login_withValidCredentials_returnsBearerToken() throws Exception {
        mockMvc.perform(loginRequest(DOCTOR, "doctor-pass"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(jwtProperties.expirationMinutes() * 60));
    }

    @Test
    void login_withWrongPassword_returns401WithGenericMessage() throws Exception {
        mockMvc.perform(loginRequest(DOCTOR, "wrong-password"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid username or password"))
                .andExpect(jsonPath("$.accessToken").doesNotExist());
    }

    @Test
    void login_withUnknownUser_returnsSameResponseAsWrongPassword() throws Exception {
        // Same status and message as a wrong password: the API does not reveal which usernames exist.
        mockMvc.perform(loginRequest("nobody", "whatever"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void login_withDisabledUser_returns401() throws Exception {
        mockMvc.perform(loginRequest(DISABLED, "disabled-pass"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void login_withBlankFields_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------- 401: no / bad token

    @Test
    void protectedEndpoint_withoutToken_returns401Json() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/auth/me"));
    }

    @Test
    void protectedEndpoint_withGarbageToken_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withExpiredToken_returns401() throws Exception {
        // A token created one hour ago, valid for only 15 minutes.
        Clock anHourAgo = Clock.fixed(Instant.now().minus(1, ChronoUnit.HOURS), ZoneOffset.UTC);
        String expired = new JwtService(jwtProperties, anHourAgo).generateToken(DOCTOR, Role.DOCTOR);

        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, bearer(expired)))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------- 403: wrong role

    @Test
    void doctorOnlyEndpoint_withAssistantToken_returns403Json() throws Exception {
        mockMvc.perform(authorized(get("/api/test/doctor-area"), ASSISTANT, Role.ASSISTANT))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    // ------------------------------------------------------- 200: valid token

    @Test
    void doctorOnlyEndpoint_withDoctorToken_returns200() throws Exception {
        mockMvc.perform(authorized(get("/api/test/doctor-area"), DOCTOR, Role.DOCTOR))
                .andExpect(status().isOk())
                .andExpect(content().string("doctor-area"));
    }

    @Test
    void assistantEndpoint_withAssistantToken_returns200() throws Exception {
        mockMvc.perform(authorized(get("/api/test/assistant-area"), ASSISTANT, Role.ASSISTANT))
                .andExpect(status().isOk());
    }

    @Test
    void assistantEndpoint_withDoctorToken_returns200_becauseDoctorInheritsAssistant() throws Exception {
        // This is the role hierarchy at work: ROLE_DOCTOR > ROLE_ASSISTANT.
        mockMvc.perform(authorized(get("/api/test/assistant-area"), DOCTOR, Role.DOCTOR))
                .andExpect(status().isOk());
    }

    @Test
    void me_withValidToken_returnsUsernameAndRole() throws Exception {
        mockMvc.perform(authorized(get("/api/auth/me"), ASSISTANT, Role.ASSISTANT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(ASSISTANT))
                .andExpect(jsonPath("$.role").value("ASSISTANT"));
    }

    // ------------------------------------------------------------ full flow

    @Test
    void fullFlow_loginThenCallProtectedEndpointWithTheReturnedToken() throws Exception {
        String body = mockMvc.perform(loginRequest(DOCTOR, "doctor-pass"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(body, "$.accessToken");
        assertThat(token).isNotBlank();

        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(DOCTOR))
                .andExpect(jsonPath("$.role").value("DOCTOR"));
    }

    // -------------------------------------------------------------- helpers

    private void saveUser(String username, String rawPassword, Role role, boolean enabled) {
        userRepository.save(User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .fullName(username)
                .role(role)
                .enabled(enabled)
                .build());
    }

    private MockHttpServletRequestBuilder loginRequest(String username, String password) {
        return post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password));
    }

    /** Adds a valid token for the given user (made directly with JwtService, no login call). */
    private MockHttpServletRequestBuilder authorized(MockHttpServletRequestBuilder request, String username, Role role) {
        return request.header(HttpHeaders.AUTHORIZATION, bearer(jwtService.generateToken(username, role)));
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
