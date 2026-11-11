package com.clinic.doctor_office.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Runs on every request. If the request carries "Authorization: Bearer <token>" and the
 * token is valid, it puts the user in the SecurityContext. Otherwise it does nothing:
 * the request simply stays anonymous and Spring Security answers 401 later if the
 * endpoint needs authentication. The filter never rejects a request itself.
 *
 * It trusts the token (no database lookup), which keeps the API stateless. Consequence:
 * a user disabled in the database can still use an already-issued token until it
 * expires (15 minutes by default).
 *
 * IMPORTANT: this class is deliberately NOT a Spring bean. A Filter bean is also
 * auto-registered in the servlet container, outside the security chain, where it
 * would run too early and its authentication would be wiped. SecurityConfig creates it
 * with "new" and adds it to the chain only.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length()).trim();
            jwtService.parse(token).ifPresent(user -> authenticate(user, request));
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(AuthenticatedUser user, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name())));
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
}
