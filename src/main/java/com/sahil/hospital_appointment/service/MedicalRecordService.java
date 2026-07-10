package com.sahil.hospital_appointment.service;

import com.sahil.hospital_appointment.dto.request.MedicalRecordRequest;
import com.sahil.hospital_appointment.dto.response.MedicalRecordResponse;
import com.sahil.hospital_appointment.exception.DuplicateResourceException;
import com.sahil.hospital_appointment.exception.InvalidRequestException;
import com.sahil.hospital_appointment.exception.ResourceNotFoundException;
import com.sahil.hospital_appointment.model.Appointment;
import com.sahil.hospital_appointment.model.AppointmentStatus;
import com.sahil.hospital_appointment.model.MedicalRecord;
import com.sahil.hospital_appointment.repository.MedicalRecordRepository;
import com.sahil.hospital_appointment.service.mapper.MedicalRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicalRecordMapper medicalRecordMapper;
    private final AppointmentService appointmentService;

    @Transactional
    public MedicalRecordResponse createMedicalRecord(MedicalRecordRequest request) {
        Appointment appointment = appointmentService.findAppointmentOrThrow(request.getAppointmentId());

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new InvalidRequestException(
                    "Cannot create a medical record for an appointment that is not COMPLETED (current status: "
                            + appointment.getStatus() + ")");
        }

        if (medicalRecordRepository.existsByAppointmentId(appointment.getId())) {
            throw new DuplicateResourceException(
                    "A medical record already exists for appointment id: " + appointment.getId());
        }

        MedicalRecord record = MedicalRecord.builder()
                .appointment(appointment)
                .diagnosis(request.getDiagnosis())
                .prescription(request.getPrescription())
                .doctorNotes(request.getDoctorNotes())
                .build();

        MedicalRecord saved = medicalRecordRepository.save(record);
        return medicalRecordMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponse getMedicalRecordByAppointmentId(Long appointmentId) {
        MedicalRecord record = medicalRecordRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No medical record found for appointment id: " + appointmentId));
        return medicalRecordMapper.toResponse(record);
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponse getMedicalRecordById(Long id) {
        MedicalRecord record = findMedicalRecordOrThrow(id);
        return medicalRecordMapper.toResponse(record);
    }

    // ---------- Helpers ----------

    @Transactional(readOnly = true)
    public MedicalRecord findMedicalRecordOrThrow(Long id) {
        return medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord", id));
    }

    // Used by @PreAuthorize: is the currently logged-in user the DOCTOR
    // who conducted the appointment this record belongs to?
    @Transactional(readOnly = true)
    public boolean isRecordDoctor(Long appointmentId, String currentUserEmail) {
        return appointmentService.isDoctorOwner(appointmentId, currentUserEmail);
    }

    // Used by @PreAuthorize: is the currently logged-in user the PATIENT
    // this record belongs to?
    @Transactional(readOnly = true)
    public boolean isRecordPatient(Long appointmentId, String currentUserEmail) {
        return appointmentService.isPatientOwner(appointmentId, currentUserEmail);
    }
}