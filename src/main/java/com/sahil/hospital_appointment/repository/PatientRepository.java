package com.sahil.hospital_appointment.repository;

import com.sahil.hospital_appointment.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByUserEmail(Long userId);

    Optional<Patient> findByUserEmail(String email);

}
