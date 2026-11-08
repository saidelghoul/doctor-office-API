package com.clinic.doctor_office.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Column renamed: "date" is a SQL type keyword, avoid any dialect trouble.
    @Column(name = "holiday_date")
    private LocalDate date;

    private String label;
}
