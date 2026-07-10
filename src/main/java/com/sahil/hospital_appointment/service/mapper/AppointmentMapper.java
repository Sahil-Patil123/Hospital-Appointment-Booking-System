package com.sahil.hospital_appointment.service.mapper;

import com.sahil.hospital_appointment.dto.response.AppointmentResponse;
import com.sahil.hospital_appointment.model.Appointment;
import org.springframework.stereotype.Component;

/**
 * Converts Appointment entities into AppointmentResponse DTOs. Flattens
 * Doctor/Patient names directly onto the response so a frontend can show
 * "Dr. Smith with John Doe" without extra API calls.
 */
@Component
public class AppointmentMapper {

    public AppointmentResponse toResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .doctorId(appointment.getDoctor().getId())
                .doctorName(appointment.getDoctor().getUser().getFullName())
                .doctorSpecialization(appointment.getDoctor().getSpecialization())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getUser().getFullName())
                .appointmentDate(appointment.getAppointmentDate())
                .appointmentTime(appointment.getAppointmentTime())
                .status(appointment.getStatus())
                .reasonForVisit(appointment.getReasonForVisit())
                .cancellationReason(appointment.getCancellationReason())
                .build();
    }
}