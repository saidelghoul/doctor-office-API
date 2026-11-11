package com.clinic.doctor_office.dto;

import com.clinic.doctor_office.domain.Role;

/** Returned by GET /api/auth/me. */
public record CurrentUserResponse(String username, Role role) {
}
