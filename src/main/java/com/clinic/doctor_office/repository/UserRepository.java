package com.clinic.doctor_office.repository;

import com.clinic.doctor_office.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
