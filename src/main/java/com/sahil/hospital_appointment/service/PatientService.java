package com.sahil.hospital_appointment.service;

import com.sahil.hospital_appointment.dto.request.PatientProfileRequest;
import com.sahil.hospital_appointment.dto.response.PatientResponse;
import com.sahil.hospital_appointment.exception.ResourceNotFoundException;
import com.sahil.hospital_appointment.model.Patient;
import com.sahil.hospital_appointment.repository.PatientRepository;
import com.sahil.hospital_appointment.service.mapper.PatientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for Patient CRUD. Same layering rule as DoctorService
 * (Step 8): ownership/role checks live in the Controller via
 * @PreAuthorize, this Service just does data operations.
 */
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    @Transactional(readOnly = true)
    public com.sahil.hospital_appointment.dto.PageResponse<PatientResponse> getAllPatients(
            org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<PatientResponse> page =
                patientRepository.findAll(pageable).map(patientMapper::toResponse);
        return com.sahil.hospital_appointment.dto.PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long id) {
        Patient patient = findPatientOrThrow(id);
        return patientMapper.toResponse(patient);
    }

    @Transactional
    public PatientResponse updatePatientProfile(Long id, PatientProfileRequest request) {
        Patient patient = findPatientOrThrow(id);

        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setAddress(request.getAddress());
        patient.setMedicalHistory(request.getMedicalHistory());

        Patient updated = patientRepository.save(patient);
        return patientMapper.toResponse(updated);
    }

    @Transactional
    public void deletePatient(Long id) {
        Patient patient = findPatientOrThrow(id);
        patientRepository.delete(patient);
    }

    // Used by other Services (e.g. AppointmentService in Step 12) that
    // need the raw entity, not the response DTO
    @Transactional(readOnly = true)
    public Patient findPatientOrThrow(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
    }

    // Used for authorization checks in the Controller: "does this patient
    // record belong to the currently logged-in user?"
    @Transactional(readOnly = true)
    public boolean isOwner(Long patientId, String currentUserEmail) {
        return patientRepository.findById(patientId)
                .map(patient -> patient.getUser().getEmail().equals(currentUserEmail))
                .orElse(false);
    }
}