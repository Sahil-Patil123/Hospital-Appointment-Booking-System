package com.sahil.hospital_appointment.repository;

import com.sahil.hospital_appointment.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long>, JpaSpecificationExecutor<Doctor> {
    Optional<Doctor> findByUserId(Long userId);

    Optional<Doctor> findByUserEmail(String email);

    java.util.List<Doctor> findBySpecializationIgnoreCase(String specialization);
}
