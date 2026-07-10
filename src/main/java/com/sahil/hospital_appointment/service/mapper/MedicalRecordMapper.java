package com.sahil.hospital_appointment.service.mapper;

import com.sahil.hospital_appointment.dto.response.MedicalRecordResponse;
import com.sahil.hospital_appointment.model.MedicalRecord;
import org.springframework.stereotype.Component;

@Component
public class MedicalRecordMapper {

    public MedicalRecordResponse toResponse(MedicalRecord record) {
        return MedicalRecordResponse.builder()
                .id(record.getId())
                .appointmentId(record.getAppointment().getId())
                .diagnosis(record.getDiagnosis())
                .prescription(record.getPrescription())
                .doctorNotes(record.getDoctorNotes())
                .createdAt(record.getCreatedAt())
                .build();
    }
}