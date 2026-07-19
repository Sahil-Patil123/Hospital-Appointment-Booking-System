package com.sahil.hospital_appointment.controller;

import com.sahil.hospital_appointment.dto.ApiResponse;
import com.sahil.hospital_appointment.dto.PageResponse;
import com.sahil.hospital_appointment.dto.request.PatientProfileRequest;
import com.sahil.hospital_appointment.dto.response.PatientResponse;
import com.sahil.hospital_appointment.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<PageResponse<PatientResponse>>> getAllPatients(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        var patients = patientService.getAllPatients(pageable);
        return ResponseEntity.ok(ApiResponse.success("Patients fetched successfully", patients));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientService.isOwner(#id, authentication.name)")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientById(@PathVariable Long id) {
        PatientResponse patient = patientService.getPatientById(id);
        return ResponseEntity.ok(ApiResponse.success("Patient fetched successfully", patient));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @patientService.isOwner(#id, authentication.name)")
    public ResponseEntity<ApiResponse<PatientResponse>> updatePatientProfile(
            @PathVariable Long id,
            @Valid @RequestBody PatientProfileRequest request) {
        PatientResponse updated = patientService.updatePatientProfile(id, request);
        return ResponseEntity.ok(ApiResponse.success("Patient profile updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok(ApiResponse.success("Patient deleted successfully"));
    }
}