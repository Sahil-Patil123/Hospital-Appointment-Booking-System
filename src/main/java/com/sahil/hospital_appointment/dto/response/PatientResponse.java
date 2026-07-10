package com.sahil.hospital_appointment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientResponse {
    private Long id;
    private String fullName;   // pulled from linked User
    private String email;      // pulled from linked User
    private LocalDate dateOfBirth;
    private String gender;
    private String phoneNumber;
    private String address;
    private String medicalHistory;
}
