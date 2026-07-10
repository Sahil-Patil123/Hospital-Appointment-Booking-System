package com.sahil.hospital_appointment.dto.response;

import com.sahil.hospital_appointment.model.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private Long patientId;
    private String patientName;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private AppointmentStatus status;
    private String reasonForVisit;
    private String cancellationReason;
}
