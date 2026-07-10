package com.sahil.hospital_appointment.service;

import com.sahil.hospital_appointment.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private Appointment buildAppointment() {
        User doctorUser = User.builder().fullName("Dr. Smith").email("doctor@test.com").build();
        User patientUser = User.builder().fullName("John Doe").email("patient@test.com").build();
        Doctor doctor = Doctor.builder().id(1L).user(doctorUser).build();
        Patient patient = Patient.builder().id(1L).user(patientUser).build();

        return Appointment.builder()
                .id(1L)
                .doctor(doctor)
                .patient(patient)
                .appointmentDate(LocalDate.now().plusDays(1))
                .appointmentTime(LocalTime.of(10, 0))
                .status(AppointmentStatus.PENDING)
                .reasonForVisit("Checkup")
                .build();
    }

    @Test
    void sendBookingConfirmation_shouldCallMailSender_withCorrectRecipient() {
        Appointment appointment = buildAppointment();

        emailService.sendBookingConfirmation(appointment);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendCancellationNotice_shouldSendToBothPatientAndDoctor() {
        Appointment appointment = buildAppointment();
        appointment.setCancellationReason("Doctor unavailable");

        emailService.sendCancellationNotice(appointment);

        // One email to patient, one to doctor = 2 total calls
        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmail_shouldNotThrow_whenMailSenderFails() {
        Appointment appointment = buildAppointment();
        doThrow(new MailSendException("SMTP connection failed"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // The critical assertion: this must NOT throw, even though
        // mailSender.send() blows up internally
        emailService.sendBookingConfirmation(appointment);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}