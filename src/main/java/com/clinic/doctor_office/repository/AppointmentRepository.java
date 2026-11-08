package com.clinic.doctor_office.repository;

import com.clinic.doctor_office.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
}
