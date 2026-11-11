package com.clinic.doctor_office.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Called by Spring Security when an anonymous request hits a protected endpoint
 * (missing, invalid or expired token). Security filters run before Spring MVC, so
 * @RestControllerAdvice would not see this exception on its own; we hand it over to MVC's
 * exception resolver, which finds SecurityExceptionHandler and writes the 401 JSON.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final HandlerExceptionResolver resolver;

    // Explicit constructor because of @Qualifier: Boot has several HandlerExceptionResolver beans.
    public RestAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) {
        resolver.resolveException(request, response, null, authException);
    }
}
