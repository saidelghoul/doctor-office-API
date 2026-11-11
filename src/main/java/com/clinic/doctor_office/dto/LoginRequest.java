package com.clinic.doctor_office.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String username, @NotBlank String password) {

    // The generated toString() would print the password; keep it out of logs.
    @Override
    public String toString() {
        return "LoginRequest[username=" + username + ", password=***]";
    }
}
