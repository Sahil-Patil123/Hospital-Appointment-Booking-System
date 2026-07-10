package com.sahil.hospital_appointment.controller;

import com.sahil.hospital_appointment.dto.ApiResponse;
import com.sahil.hospital_appointment.dto.request.PatientProfileRequest;
import com.sahil.hospital_appointment.dto.response.PatientResponse;
import com.sahil.hospital_appointment.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Patient CRUD endpoints. Unlike Doctor (viewable by everyone), patient
 * records are more sensitive - a PATIENT should only see/edit their own
 * data, while ADMIN and DOCTOR need broader visibility for care delivery.
 */
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    // Only ADMIN and DOCTOR can browse the full patient list - a PATIENT
    // has no legitimate reason to see every other patient's data
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<com.sahil.hospital_appointment.dto.PageResponse<PatientResponse>>> getAllPatients(
            @org.springframework.data.web.PageableDefault(size = 10, sort = "id") org.springframework.data.domain.Pageable pageable) {
        var patients = patientService.getAllPatients(pageable);
        return ResponseEntity.ok(ApiResponse.success("Patients fetched successfully", patients));
    }

    // ADMIN/DOCTOR can view any patient; a PATIENT can only view themself
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientService.isOwner(#id, authentication.name)")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientById(@PathVariable Long id) {
        PatientResponse patient = patientService.getPatientById(id);
        return ResponseEntity.ok(ApiResponse.success("Patient fetched successfully", patient));
    }

    // Only ADMIN or the patient themself can update their profile -
    // a DOCTOR should NOT be able to edit a patient's personal details
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @patientService.isOwner(#id, authentication.name)")
    public ResponseEntity<ApiResponse<PatientResponse>> updatePatientProfile(
            @PathVariable Long id,
            @Valid @RequestBody PatientProfileRequest request) {
        PatientResponse updated = patientService.updatePatientProfile(id, request);
        return ResponseEntity.ok(ApiResponse.success("Patient profile updated successfully", updated));
    }

    // Only ADMIN can delete a patient record
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok(ApiResponse.success("Patient deleted successfully"));
    }
}