package com.clinic.doctor_office.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TEST-ONLY controller: it lives in src/test, so it is never part of the real application.
 * It gives the integration tests two role-protected endpoints to call until real ones exist.
 * (Spring finds it because it is under the application's base package on the test classpath.)
 */
@RestController
@RequestMapping("/api/test")
public class TestSecuredController {

    @GetMapping("/assistant-area")
    @PreAuthorize("hasRole('ASSISTANT')")
    public String assistantArea() {
        return "assistant-area";
    }

    @GetMapping("/doctor-area")
    @PreAuthorize("hasRole('DOCTOR')")
    public String doctorArea() {
        return "doctor-area";
    }
}
