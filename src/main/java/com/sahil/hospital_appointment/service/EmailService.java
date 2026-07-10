package com.sahil.hospital_appointment.service;

import com.sahil.hospital_appointment.model.Appointment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Sends email notifications for appointment lifecycle events. Every
 * method is @Async - the caller (AppointmentService) fires and forgets,
 * never blocking the API response waiting for an email to actually send.
 *
 * IMPORTANT: every method swallows its own exceptions (logs instead of
 * throwing) - a failed email must NEVER roll back or fail the underlying
 * booking/status-change transaction that triggered it.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

    @Async
    public void sendBookingConfirmation(Appointment appointment) {
        String patientEmail = appointment.getPatient().getUser().getEmail();
        String doctorName = appointment.getDoctor().getUser().getFullName();

        String subject = "Appointment Booked - Hospital Appointment System";
        String body = String.format(
                "Dear %s,%n%nYour appointment with Dr. %s has been booked and is PENDING confirmation.%n%n"
                        + "Date: %s%nTime: %s%nReason: %s%n%n"
                        + "You will receive another email once the doctor confirms your appointment.%n%n"
                        + "Thank you for using our Hospital Appointment System.",
                appointment.getPatient().getUser().getFullName(),
                doctorName,
                appointment.getAppointmentDate().format(DATE_FORMAT),
                appointment.getAppointmentTime().format(TIME_FORMAT),
                appointment.getReasonForVisit() != null ? appointment.getReasonForVisit() : "Not specified"
        );

        sendEmail(patientEmail, subject, body);
    }

    @Async
    public void sendConfirmationNotice(Appointment appointment) {
        String patientEmail = appointment.getPatient().getUser().getEmail();
        String doctorName = appointment.getDoctor().getUser().getFullName();

        String subject = "Appointment Confirmed - Hospital Appointment System";
        String body = String.format(
                "Dear %s,%n%nGood news! Dr. %s has CONFIRMED your appointment.%n%n"
                        + "Date: %s%nTime: %s%n%nPlease arrive 10 minutes early.%n%n"
                        + "Thank you for using our Hospital Appointment System.",
                appointment.getPatient().getUser().getFullName(),
                doctorName,
                appointment.getAppointmentDate().format(DATE_FORMAT),
                appointment.getAppointmentTime().format(TIME_FORMAT)
        );

        sendEmail(patientEmail, subject, body);
    }

    @Async
    public void sendCancellationNotice(Appointment appointment) {
        String patientEmail = appointment.getPatient().getUser().getEmail();
        String doctorEmail = appointment.getDoctor().getUser().getEmail();
        String doctorName = appointment.getDoctor().getUser().getFullName();
        String patientName = appointment.getPatient().getUser().getFullName();

        String subject = "Appointment Cancelled - Hospital Appointment System";

        String patientBody = String.format(
                "Dear %s,%n%nYour appointment with Dr. %s on %s at %s has been CANCELLED.%n%n"
                        + "Reason: %s%n%nPlease book a new appointment if needed.%n%n"
                        + "Thank you for using our Hospital Appointment System.",
                patientName, doctorName,
                appointment.getAppointmentDate().format(DATE_FORMAT),
                appointment.getAppointmentTime().format(TIME_FORMAT),
                appointment.getCancellationReason() != null ? appointment.getCancellationReason() : "Not specified"
        );

        String doctorBody = String.format(
                "Dear Dr. %s,%n%nThe appointment with %s on %s at %s has been CANCELLED.%n%n"
                        + "Reason: %s%n%nThis slot is now available for other bookings.",
                doctorName, patientName,
                appointment.getAppointmentDate().format(DATE_FORMAT),
                appointment.getAppointmentTime().format(TIME_FORMAT),
                appointment.getCancellationReason() != null ? appointment.getCancellationReason() : "Not specified"
        );

        sendEmail(patientEmail, subject, patientBody);
        sendEmail(doctorEmail, subject, doctorBody);
    }

    @Async
    public void sendCompletionNotice(Appointment appointment) {
        String patientEmail = appointment.getPatient().getUser().getEmail();
        String doctorName = appointment.getDoctor().getUser().getFullName();

        String subject = "Appointment Completed - Hospital Appointment System";
        String body = String.format(
                "Dear %s,%n%nYour appointment with Dr. %s on %s has been marked as COMPLETED.%n%n"
                        + "If a medical record was created, you can view it in your patient dashboard.%n%n"
                        + "Thank you for using our Hospital Appointment System.",
                appointment.getPatient().getUser().getFullName(),
                doctorName,
                appointment.getAppointmentDate().format(DATE_FORMAT)
        );

        sendEmail(patientEmail, subject, body);
    }

    // ---------- Private helper ----------

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (Exception ex) {
            // Deliberately caught here, NOT re-thrown - a failed email
            // must never break the calling transaction (booking/status
            // change already succeeded and was already committed by the
            // time this async method runs)
            log.error("Failed to send email to {}: {}", to, ex.getMessage());
        }
    }
}