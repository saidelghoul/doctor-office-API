package com.clinic.exception;

// Placeholder: will be thrown by PatientService and mapped to HTTP 404 later.
public class PatientNotFoundException extends RuntimeException {

    public PatientNotFoundException(Long id) {
        super("Patient not found: " + id);
    }
}
