package com.sahil.hospital_appointment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotBlank(message = "Specoalization is required")
    @Column(nullable = false)
    private String specialization;

    @NotBlank(message = "Qualification is required")
    @Column(nullable = false)
    private String qualification;

    @Column(nullable = false)
    private Integer yearsOfExperience;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private Double consultationFee;

    @OneToMany(mappedBy = "doctor",cascade = CascadeType.ALL,orphanRemoval = true)
    @Builder.Default
    private List<DoctorSchedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "doctor")
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();



}
