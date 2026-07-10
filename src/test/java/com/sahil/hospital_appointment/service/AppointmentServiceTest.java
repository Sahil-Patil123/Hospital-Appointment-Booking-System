package com.sahil.hospital_appointment.service;

import com.sahil.hospital_appointment.dto.request.AppointmentRequest;
import com.sahil.hospital_appointment.dto.response.AppointmentResponse;
import com.sahil.hospital_appointment.exception.InvalidRequestException;
import com.sahil.hospital_appointment.exception.ResourceNotFoundException;
import com.sahil.hospital_appointment.model.Appointment;
import com.sahil.hospital_appointment.model.AppointmentStatus;
import com.sahil.hospital_appointment.model.Doctor;
import com.sahil.hospital_appointment.model.User;
import com.sahil.hospital_appointment.repository.AppointmentRepository;
import com.sahil.hospital_appointment.service.mapper.AppointmentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentStatusFlowTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private AppointmentMapper appointmentMapper;
    @Mock private DoctorService doctorService;
    @Mock private PatientService patientService;
    @Mock private SlotService slotService;
    @Mock private EmailService emailService;

    @InjectMocks
    private AppointmentService appointmentService;

    private Appointment buildAppointment(AppointmentStatus status) {
        return Appointment.builder().id(1L).status(status).build();
    }

    private Doctor buildDoctor() {
        User user = User.builder().id(1L).fullName("Dr. Test").email("doc@test.com").build();
        return Doctor.builder()
                .id(1L)
                .user(user)
                .specialization("Cardiology")
                .qualification("MD")
                .yearsOfExperience(5)
                .phoneNumber("9999999999")
                .consultationFee(400.0)
                .build();
    }

    private void stubMapperPassthrough() {
        when(appointmentMapper.toResponse(any(Appointment.class))).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            return AppointmentResponse.builder().id(a.getId()).status(a.getStatus()).build();
        });
    }

    @Test
    void confirmAppointment_shouldSucceed_whenCurrentlyPending() {
        Appointment appointment = buildAppointment(AppointmentStatus.PENDING);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(appointment);
        stubMapperPassthrough();

        AppointmentResponse result = appointmentService.confirmAppointment(1L);

        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
    }

    @Test
    void confirmAppointment_shouldThrow_whenNotPending() {
        Appointment appointment = buildAppointment(AppointmentStatus.COMPLETED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.confirmAppointment(1L))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Cannot change appointment status from COMPLETED to CONFIRMED");
    }

    @Test
    void completeAppointment_shouldSucceed_whenCurrentlyConfirmed() {
        Appointment appointment = buildAppointment(AppointmentStatus.CONFIRMED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(appointment);
        stubMapperPassthrough();

        AppointmentResponse result = appointmentService.completeAppointment(1L);

        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
    }

    @Test
    void completeAppointment_shouldThrow_whenStillPending() {
        Appointment appointment = buildAppointment(AppointmentStatus.PENDING);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.completeAppointment(1L))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Cannot change appointment status from PENDING to COMPLETED");
    }

    @Test
    void cancelAppointment_shouldSucceed_whenPending() {
        Appointment appointment = buildAppointment(AppointmentStatus.PENDING);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(appointment);
        stubMapperPassthrough();

        AppointmentResponse result = appointmentService.cancelAppointment(1L, "Patient requested");

        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    @Test
    void cancelAppointment_shouldSucceed_whenConfirmed() {
        Appointment appointment = buildAppointment(AppointmentStatus.CONFIRMED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(appointment);
        stubMapperPassthrough();

        AppointmentResponse result = appointmentService.cancelAppointment(1L, "Doctor unavailable");

        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    @Test
    void cancelAppointment_shouldThrow_whenAlreadyCompleted() {
        Appointment appointment = buildAppointment(AppointmentStatus.COMPLETED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancelAppointment(1L, "Too late"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Cannot change appointment status from COMPLETED to CANCELLED");
    }

    @Test
    void cancelAppointment_shouldThrow_whenAlreadyCancelled() {
        Appointment appointment = buildAppointment(AppointmentStatus.CANCELLED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancelAppointment(1L, "Duplicate cancel"))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void bookAppointment_shouldPropagateResourceNotFoundException_whenDoctorMissing() {
        AppointmentRequest request = new AppointmentRequest(99L,
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), "Checkup");

        when(doctorService.findDoctorOrThrow(99L))
                .thenThrow(new ResourceNotFoundException("Doctor", 99L));

        assertThatThrownBy(() -> appointmentService.bookAppointment(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(appointmentRepository, never()).save(any());
        verify(patientService, never()).findPatientOrThrow(any());
    }

    @Test
    void bookAppointment_shouldPropagateResourceNotFoundException_whenPatientMissing() {
        Doctor doctor = buildDoctor();
        AppointmentRequest request = new AppointmentRequest(1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0), "Checkup");

        when(doctorService.findDoctorOrThrow(1L)).thenReturn(doctor);
        when(patientService.findPatientOrThrow(99L))
                .thenThrow(new ResourceNotFoundException("Patient", 99L));

        assertThatThrownBy(() -> appointmentService.bookAppointment(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void isPatientSelf_shouldDelegateToPatientServiceIsOwner() {
        when(patientService.isOwner(1L, "patient@test.com")).thenReturn(true);

        assertThat(appointmentService.isPatientSelf(1L, "patient@test.com")).isTrue();
        verify(patientService).isOwner(1L, "patient@test.com");
    }

}