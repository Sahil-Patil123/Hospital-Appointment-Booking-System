package com.sahil.hospital_appointment.service;

import com.sahil.hospital_appointment.dto.request.MedicalRecordRequest;
import com.sahil.hospital_appointment.dto.response.MedicalRecordResponse;
import com.sahil.hospital_appointment.exception.DuplicateResourceException;
import com.sahil.hospital_appointment.exception.InvalidRequestException;
import com.sahil.hospital_appointment.model.Appointment;
import com.sahil.hospital_appointment.model.AppointmentStatus;
import com.sahil.hospital_appointment.model.MedicalRecord;
import com.sahil.hospital_appointment.repository.MedicalRecordRepository;
import com.sahil.hospital_appointment.service.mapper.MedicalRecordMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {

    @Mock private MedicalRecordRepository medicalRecordRepository;
    @Mock private MedicalRecordMapper medicalRecordMapper;
    @Mock private AppointmentService appointmentService;

    @InjectMocks
    private MedicalRecordService medicalRecordService;

    private Appointment buildAppointment(AppointmentStatus status) {
        return Appointment.builder().id(1L).status(status).build();
    }

    @Test
    void createMedicalRecord_shouldSucceed_whenAppointmentCompletedAndNoExistingRecord() {
        Appointment appointment = buildAppointment(AppointmentStatus.COMPLETED);
        MedicalRecordRequest request = new MedicalRecordRequest(1L, "Flu", "Paracetamol", "Rest advised");

        when(appointmentService.findAppointmentOrThrow(1L)).thenReturn(appointment);
        when(medicalRecordRepository.existsByAppointmentId(1L)).thenReturn(false);
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenAnswer(inv -> {
            MedicalRecord r = inv.getArgument(0);
            r.setId(10L);
            return r;
        });
        when(medicalRecordMapper.toResponse(any(MedicalRecord.class))).thenAnswer(inv -> {
            MedicalRecord r = inv.getArgument(0);
            return MedicalRecordResponse.builder().id(r.getId()).diagnosis(r.getDiagnosis()).build();
        });

        MedicalRecordResponse result = medicalRecordService.createMedicalRecord(request);

        assertThat(result.getDiagnosis()).isEqualTo("Flu");
    }

    @Test
    void createMedicalRecord_shouldThrowInvalidRequestException_whenAppointmentNotCompleted() {
        Appointment appointment = buildAppointment(AppointmentStatus.CONFIRMED);
        MedicalRecordRequest request = new MedicalRecordRequest(1L, "Flu", "Paracetamol", "Rest advised");

        when(appointmentService.findAppointmentOrThrow(1L)).thenReturn(appointment);

        assertThatThrownBy(() -> medicalRecordService.createMedicalRecord(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("not COMPLETED");
    }

    @Test
    void createMedicalRecord_shouldThrowDuplicateResourceException_whenRecordAlreadyExists() {
        Appointment appointment = buildAppointment(AppointmentStatus.COMPLETED);
        MedicalRecordRequest request = new MedicalRecordRequest(1L, "Flu", "Paracetamol", "Rest advised");

        when(appointmentService.findAppointmentOrThrow(1L)).thenReturn(appointment);
        when(medicalRecordRepository.existsByAppointmentId(1L)).thenReturn(true);

        assertThatThrownBy(() -> medicalRecordService.createMedicalRecord(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void getMedicalRecordByAppointmentId_shouldThrowResourceNotFoundException_whenMissing() {
        when(medicalRecordRepository.findByAppointmentId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalRecordService.getMedicalRecordByAppointmentId(99L))
                .isInstanceOf(com.sahil.hospital_appointment.exception.ResourceNotFoundException.class);
    }

    @Test
    void isRecordDoctor_shouldDelegateToAppointmentService() {
        when(appointmentService.isDoctorOwner(1L, "doc@test.com")).thenReturn(true);

        assertThat(medicalRecordService.isRecordDoctor(1L, "doc@test.com")).isTrue();
    }
}