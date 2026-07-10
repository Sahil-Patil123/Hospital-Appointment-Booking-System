package com.sahil.hospital_appointment.service.mapper;

import com.sahil.hospital_appointment.dto.response.DoctorResponse;
import com.sahil.hospital_appointment.model.Doctor;
import org.springframework.stereotype.Component;

@Component
public class DoctorMapper {

    public DoctorResponse toResponse(Doctor doctor) {
        return DoctorResponse.builder()
                .id(doctor.getId())
                .fullName(doctor.getUser().getFullName())
                .email(doctor.getUser().getEmail())
                .specialization(doctor.getSpecialization())
                .qualification(doctor.getQualification())
                .yearsOfExperience(doctor.getYearsOfExperience())
                .phoneNumber(doctor.getPhoneNumber())
                .consultationFee(doctor.getConsultationFee())
                .build();
    }
}