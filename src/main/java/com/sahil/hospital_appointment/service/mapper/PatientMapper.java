package com.sahil.hospital_appointment.service.mapper;

import com.sahil.hospital_appointment.dto.response.PatientResponse;
import com.sahil.hospital_appointment.model.Patient;
import org.springframework.stereotype.Component;

/**
 * Converts Patient entities into PatientResponse DTOs - same pattern as
 * DoctorMapper (Step 8).
 */
@Component
public class PatientMapper {

    public PatientResponse toResponse(Patient patient) {
        return PatientResponse.builder()
                .id(patient.getId())
                .fullName(patient.getUser().getFullName())
                .email(patient.getUser().getEmail())
                .dateOfBirth(patient.getDateOfBirth())
                .gender(patient.getGender())
                .phoneNumber(patient.getPhoneNumber())
                .address(patient.getAddress())
                .medicalHistory(patient.getMedicalHistory())
                .build();
    }
}