package com.clinic.doctor_office.repository;

import com.clinic.doctor_office.domain.WorkingDay;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkingDayRepository extends JpaRepository<WorkingDay, Long> {
}
