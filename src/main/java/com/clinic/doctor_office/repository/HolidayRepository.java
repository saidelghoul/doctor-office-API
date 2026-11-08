package com.clinic.doctor_office.repository;

import com.clinic.doctor_office.domain.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {
}
