package com.sahil.hospital_appointment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecordResponse {
    private Long id;
    private Long appointmentId;
    private String diagnosis;
    private String prescription;
    private String doctorNotes;
    private LocalDateTime createdAt;
}
