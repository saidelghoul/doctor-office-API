package com.clinic.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

// One single record for the whole office (enforced later in the service layer).
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"workingDays", "holidays"})
public class OfficeConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String doctorName;

    private String specialization;

    private String phone;

    private String address;

    private int appointmentDurationMinutes;

    // Composition 1 *-- 1..7 (the 1..7 bound is not expressible in JPA; service layer later).
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "office_configuration_id")
    @Builder.Default
    private List<WorkingDay> workingDays = new ArrayList<>();

    // Composition 1 *-- 0..*
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "office_configuration_id")
    @Builder.Default
    private List<Holiday> holidays = new ArrayList<>();
}
