package com.sahil.hospital_appointment.repository;

import com.sahil.hospital_appointment.model.Role;
import com.sahil.hospital_appointment.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    java.util.List<User> findByRole(Role role);
}
