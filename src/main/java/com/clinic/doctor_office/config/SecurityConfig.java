package com.clinic.doctor_office.config;

import com.clinic.doctor_office.security.JwtAuthenticationFilter;
import com.clinic.doctor_office.security.JwtService;
import com.clinic.doctor_office.security.RestAccessDeniedHandler;
import com.clinic.doctor_office.security.RestAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * The security rules of the API.
 *  - Stateless: no HTTP session, no cookies; every request must carry its own JWT.
 *  - Only POST /api/auth/login is public; everything else needs a valid token.
 *  - @EnableMethodSecurity lets us write @PreAuthorize("hasRole('DOCTOR')") on controllers
 *    and services (used later for "only the doctor can complete an appointment").
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService jwtService,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler,
            // Same switch that turns the H2 console on in application.properties.
            // The postgres profile sets it to false, so the console is never exposed there.
            @Value("${spring.h2.console.enabled:false}") boolean h2ConsoleEnabled) throws Exception {

        PathPatternRequestMatcher.Builder paths = PathPatternRequestMatcher.withDefaults();

        http
                // CSRF protects cookie/session logins. We use a Bearer header and no session.
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(authenticationEntryPoint)   // 401
                        .accessDeniedHandler(accessDeniedHandler))            // 403
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(paths.matcher(HttpMethod.POST, "/api/auth/login")).permitAll();
                    // Spring Boot forwards errors (e.g. a 400 or 404) to /error. If /error needed a
                    // token, every error would be replaced by a 401.
                    auth.requestMatchers(paths.matcher("/error")).permitAll();
                    if (h2ConsoleEnabled) {
                        auth.requestMatchers(paths.matcher("/h2-console/**")).permitAll();
                    }
                    auth.anyRequest().authenticated();
                })
                // Our filter runs where the username/password filter would; it is created here
                // with "new" on purpose (see the comment in JwtAuthenticationFilter).
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        if (h2ConsoleEnabled) {
            // The H2 console is built with HTML frames; Spring Security blocks frames by default.
            http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        }

        return http.build();
    }

    /** BCrypt: slow, salted hashing. Used to hash seed passwords and to check logins. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * DOCTOR inherits everything ASSISTANT can do: hasRole('ASSISTANT') is also true for a
     * doctor. (static: this bean is needed very early, by method security.)
     */
    @Bean
    static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("ROLE_DOCTOR > ROLE_ASSISTANT");
    }

    /** Explicitly applies the hierarchy to @PreAuthorize expressions. */
    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        return handler;
    }

    /**
     * The object that checks a username/password. Spring builds it from our
     * DatabaseUserDetailsService and the PasswordEncoder above.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
