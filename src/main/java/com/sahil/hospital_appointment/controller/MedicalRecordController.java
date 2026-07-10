package com.sahil.hospital_appointment.controller;

import com.sahil.hospital_appointment.dto.ApiResponse;
import com.sahil.hospital_appointment.dto.request.MedicalRecordRequest;
import com.sahil.hospital_appointment.dto.response.MedicalRecordResponse;
import com.sahil.hospital_appointment.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    // Only the doctor who conducted the appointment (or ADMIN) can write
    // the medical record for it
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @medicalRecordService.isRecordDoctor(#request.appointmentId, authentication.name)")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> createMedicalRecord(
            @Valid @RequestBody MedicalRecordRequest request) {
        MedicalRecordResponse created = medicalRecordService.createMedicalRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Medical record created successfully", created));
    }

    // The patient it belongs to, the doctor who wrote it, or ADMIN can view it
    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasRole('ADMIN') " +
            "or @medicalRecordService.isRecordPatient(#appointmentId, authentication.name) " +
            "or @medicalRecordService.isRecordDoctor(#appointmentId, authentication.name)")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> getByAppointmentId(
            @PathVariable Long appointmentId) {
        MedicalRecordResponse record = medicalRecordService.getMedicalRecordByAppointmentId(appointmentId);
        return ResponseEntity.ok(ApiResponse.success("Medical record fetched successfully", record));
    }
}