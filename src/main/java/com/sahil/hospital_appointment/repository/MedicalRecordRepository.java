package com.sahil.hospital_appointment.repository;

import com.sahil.hospital_appointment.model.DoctorSchedule;
import com.sahil.hospital_appointment.model.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    Optional<MedicalRecord> findByAppointmentId(Long appointmentId);

    Page<MedicalRecord> findByAppointment_Patient_Id(Long patientId, Pageable pageable);

    boolean existsByAppointmentId(Long appointmentId);
}
