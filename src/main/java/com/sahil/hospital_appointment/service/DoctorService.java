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

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;

    @Transactional(readOnly = true)
    public PageResponse<DoctorResponse> getAllDoctors(Pageable pageable) {
        Page<DoctorResponse> page = doctorRepository.findAll(pageable).map(doctorMapper::toResponse);
        return PageResponse.from(page);
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

    @Transactional(readOnly = true)
    public Doctor findDoctorOrThrow(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
    }

    @Transactional(readOnly = true)
    public boolean isOwner(Long doctorId, String currentUserEmail) {
        return doctorRepository.findById(doctorId)
                .map(doctor -> doctor.getUser().getEmail().equals(currentUserEmail))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public PageResponse<DoctorResponse> searchDoctors(String specialization, Integer minExperience, Double maxFee, Pageable pageable) {
        var spec = DoctorSpecification.buildSearchSpecification(specialization, minExperience, maxFee);
        Page<DoctorResponse> page = doctorRepository.findAll(spec, pageable).map(doctorMapper::toResponse);
        return PageResponse.from(page);
    }
}