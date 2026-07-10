package com.sahil.hospital_appointment.service.specification;

import com.sahil.hospital_appointment.model.Appointment;
import com.sahil.hospital_appointment.model.AppointmentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * Same dynamic-filtering pattern as DoctorSpecification, applied to
 * Appointment search: by status, date range, doctor, or patient - any
 * combination, driven entirely by which query params were provided.
 */
public class AppointmentSpecification {

    public static Specification<Appointment> hasStatus(AppointmentStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return null;
            }
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Appointment> dateFrom(LocalDate fromDate) {
        return (root, query, cb) -> {
            if (fromDate == null) {
                return null;
            }
            return cb.greaterThanOrEqualTo(root.get("appointmentDate"), fromDate);
        };
    }

    public static Specification<Appointment> dateTo(LocalDate toDate) {
        return (root, query, cb) -> {
            if (toDate == null) {
                return null;
            }
            return cb.lessThanOrEqualTo(root.get("appointmentDate"), toDate);
        };
    }

    public static Specification<Appointment> belongsToDoctor(Long doctorId) {
        return (root, query, cb) -> {
            if (doctorId == null) {
                return null;
            }
            return cb.equal(root.get("doctor").get("id"), doctorId);
        };
    }

    public static Specification<Appointment> belongsToPatient(Long patientId) {
        return (root, query, cb) -> {
            if (patientId == null) {
                return null;
            }
            return cb.equal(root.get("patient").get("id"), patientId);
        };
    }

    public static Specification<Appointment> buildSearchSpecification(
            AppointmentStatus status, LocalDate fromDate, LocalDate toDate,
            Long doctorId, Long patientId) {
        return Specification
                .where(hasStatus(status))
                .and(dateFrom(fromDate))
                .and(dateTo(toDate))
                .and(belongsToDoctor(doctorId))
                .and(belongsToPatient(patientId));
    }
}