package com.sahil.hospital_appointment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorResponse {
    private Long id;
    private String fullName;
    private String email;
    private String specialization;
    private String qualification;
    private Integer yearsOfExperience;
    private String phoneNumber;
    private Double consultationFee;
}
