package com.clinic.doctor_office.repository;

import com.clinic.doctor_office.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
}
