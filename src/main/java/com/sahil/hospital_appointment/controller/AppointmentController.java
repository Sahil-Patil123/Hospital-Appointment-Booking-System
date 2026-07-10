package com.sahil.hospital_appointment.controller;

import com.sahil.hospital_appointment.dto.ApiResponse;
import com.sahil.hospital_appointment.dto.request.AppointmentRequest;
import com.sahil.hospital_appointment.dto.request.CancelAppointmentRequest;
import com.sahil.hospital_appointment.dto.response.AppointmentResponse;
import com.sahil.hospital_appointment.model.AppointmentStatus;
import com.sahil.hospital_appointment.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.sahil.hospital_appointment.dto.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.time.LocalDate;
import java.util.List;

/**
 * Booking + viewing endpoints. Status transitions (confirm/complete/
 * cancel) live in Step 13's separate controller methods, added onto
 * this same class once we get there.
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // A patient books for themself. #patientId in the request body must
    // match their own patient id - enforced via isPatientSelf.
    @PostMapping("/patients/{patientId}")
    @PreAuthorize("hasRole('ADMIN') or @appointmentService.isPatientSelf(#patientId, authentication.name)")
    public ResponseEntity<ApiResponse<AppointmentResponse>> bookAppointment(
            @PathVariable Long patientId,
            @Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse booked = appointmentService.bookAppointment(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Appointment booked successfully", booked));
    }

    // ADMIN sees any appointment; a PATIENT/DOCTOR can only view one
    // that actually involves them
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @appointmentService.isPatientOwner(#id, authentication.name) " +
            "or @appointmentService.isDoctorOwner(#id, authentication.name)")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentById(@PathVariable Long id) {
        AppointmentResponse appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(ApiResponse.success("Appointment fetched successfully", appointment));
    }

    // A patient views their own appointment history; ADMIN can view any patient's
    @GetMapping("/patients/{patientId}")
    @PreAuthorize("hasRole('ADMIN') or @appointmentService.isPatientSelf(#patientId, authentication.name)")
    public ResponseEntity<ApiResponse<com.sahil.hospital_appointment.dto.PageResponse<AppointmentResponse>>> getAppointmentsByPatient(
            @PathVariable Long patientId,
            @org.springframework.data.web.PageableDefault(size = 10, sort = "appointmentDate") org.springframework.data.domain.Pageable pageable) {
        var appointments = appointmentService.getAppointmentsByPatient(patientId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Appointments fetched successfully", appointments));
    }

    @GetMapping("/doctors/{doctorId}")
    @PreAuthorize("hasRole('ADMIN') or @doctorService.isOwner(#doctorId, authentication.name)")
    public ResponseEntity<ApiResponse<com.sahil.hospital_appointment.dto.PageResponse<AppointmentResponse>>> getAppointmentsByDoctor(
            @PathVariable Long doctorId,
            @org.springframework.data.web.PageableDefault(size = 10, sort = "appointmentDate") org.springframework.data.domain.Pageable pageable) {
        var appointments = appointmentService.getAppointmentsByDoctor(doctorId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Appointments fetched successfully", appointments));
    }
    // Only the doctor involved (or ADMIN) can confirm a pending appointment
    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN') or @appointmentService.isDoctorOwner(#id, authentication.name)")
    public ResponseEntity<ApiResponse<AppointmentResponse>> confirmAppointment(@PathVariable Long id) {
        AppointmentResponse updated = appointmentService.confirmAppointment(id);
        return ResponseEntity.ok(ApiResponse.success("Appointment confirmed successfully", updated));
    }

    // Only the doctor involved (or ADMIN) can mark a visit as completed
    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN') or @appointmentService.isDoctorOwner(#id, authentication.name)")
    public ResponseEntity<ApiResponse<AppointmentResponse>> completeAppointment(@PathVariable Long id) {
        AppointmentResponse updated = appointmentService.completeAppointment(id);
        return ResponseEntity.ok(ApiResponse.success("Appointment marked as completed", updated));
    }

    // Either the patient or the doctor involved (or ADMIN) can cancel
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or @appointmentService.isPatientOwner(#id, authentication.name) " +
            "or @appointmentService.isDoctorOwner(#id, authentication.name)")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointment(
            @PathVariable Long id,
            @Valid @RequestBody CancelAppointmentRequest request) {
        AppointmentResponse updated = appointmentService.cancelAppointment(id, request.getCancellationReason());
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled successfully", updated));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<AppointmentResponse>>> searchAppointments(
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long patientId,
            @PageableDefault(size = 10, sort = "appointmentDate") Pageable pageable) {
        PageResponse<AppointmentResponse> results =
                appointmentService.searchAppointments(status, fromDate, toDate, doctorId, patientId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Appointment search results", results));
    }
}