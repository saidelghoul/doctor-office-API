package com.clinic.doctor_office.repository;

import com.clinic.doctor_office.domain.OfficeConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfficeConfigurationRepository extends JpaRepository<OfficeConfiguration, Long> {
}
