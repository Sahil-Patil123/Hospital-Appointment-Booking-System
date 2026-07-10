package com.sahil.hospital_appointment.service;

import com.sahil.hospital_appointment.dto.request.DoctorProfileRequest;
import com.sahil.hospital_appointment.dto.response.DoctorResponse;
import com.sahil.hospital_appointment.exception.ResourceNotFoundException;
import com.sahil.hospital_appointment.model.Doctor;
import com.sahil.hospital_appointment.repository.DoctorRepository;
import com.sahil.hospital_appointment.service.mapper.DoctorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 import com.sahil.hospital_appointment.dto.PageResponse;
 import com.sahil.hospital_appointment.service.specification.DoctorSpecification;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Business logic for Doctor CRUD. Role/ownership checks (is this MY
 * profile, am I an ADMIN) are enforced at the Controller layer via
 * @PreAuthorize - this Service assumes the caller is already authorized
 * and focuses purely on data operations.
 */
@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;

    @Transactional(readOnly = true)
    public com.sahil.hospital_appointment.dto.PageResponse<DoctorResponse> getAllDoctors(
            org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<DoctorResponse> page =
                doctorRepository.findAll(pageable).map(doctorMapper::toResponse);
        return com.sahil.hospital_appointment.dto.PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(Long id) {
        Doctor doctor = findDoctorOrThrow(id);
        return doctorMapper.toResponse(doctor);
    }

    @Transactional
    public DoctorResponse updateDoctorProfile(Long id, DoctorProfileRequest request) {
        Doctor doctor = findDoctorOrThrow(id);

        doctor.setSpecialization(request.getSpecialization());
        doctor.setQualification(request.getQualification());
        doctor.setYearsOfExperience(request.getYearsOfExperience());
        doctor.setPhoneNumber(request.getPhoneNumber());
        doctor.setConsultationFee(request.getConsultationFee());

        Doctor updated = doctorRepository.save(doctor);
        return doctorMapper.toResponse(updated);
    }

    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doctor = findDoctorOrThrow(id);
        doctorRepository.delete(doctor);
    }

    // Used by other Services (e.g. AppointmentService in Step 12) that
    // need the raw entity, not the response DTO
    @Transactional(readOnly = true)
    public Doctor findDoctorOrThrow(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
    }

    // Used for authorization checks in the Controller: "does this doctor
    // record belong to the currently logged-in user?"
    @Transactional(readOnly = true)
    public boolean isOwner(Long doctorId, String currentUserEmail) {
        return doctorRepository.findById(doctorId)
                .map(doctor -> doctor.getUser().getEmail().equals(currentUserEmail))
                .orElse(false);
    }


    @Transactional(readOnly = true)
    public PageResponse<DoctorResponse> searchDoctors(
            String specialization, Integer minExperience, Double maxFee, Pageable pageable) {
        var spec = DoctorSpecification.buildSearchSpecification(specialization, minExperience, maxFee);
        Page<DoctorResponse> page = doctorRepository.findAll(spec, pageable).map(doctorMapper::toResponse);
        return PageResponse.from(page);
    }
}