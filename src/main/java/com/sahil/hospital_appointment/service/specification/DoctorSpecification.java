package com.sahil.hospital_appointment.service.specification;

import com.sahil.hospital_appointment.model.Doctor;
import org.springframework.data.jpa.domain.Specification;

/**
 * Builds a dynamic WHERE clause for Doctor search. Each method returns
 * a Specification that can be combined with .and() - if a filter param
 * is null, its Specification returns null, which Spring Data simply
 * ignores (treats as "no condition"), so any combination of filters
 * (or none at all) works without writing a separate query per case.
 */
public class DoctorSpecification {

    public static Specification<Doctor> hasSpecialization(String specialization) {
        return (root, query, cb) -> {
            if (specialization == null || specialization.isBlank()) {
                return null; // no filter applied
            }
            // Partial, case-insensitive match: "cardio" matches "Cardiology"
            return cb.like(cb.lower(root.get("specialization")), "%" + specialization.toLowerCase() + "%");
        };
    }

    public static Specification<Doctor> hasMinExperience(Integer minExperience) {
        return (root, query, cb) -> {
            if (minExperience == null) {
                return null;
            }
            return cb.greaterThanOrEqualTo(root.get("yearsOfExperience"), minExperience);
        };
    }

    public static Specification<Doctor> hasMaxFee(Double maxFee) {
        return (root, query, cb) -> {
            if (maxFee == null) {
                return null;
            }
            return cb.lessThanOrEqualTo(root.get("consultationFee"), maxFee);
        };
    }

    // Combines every filter into one Specification. Specification.where(null)
    // starts with "no condition," and .and() safely no-ops on any null
    // Specification returned above - this is what allows any subset of
    // filters to be provided.
    public static Specification<Doctor> buildSearchSpecification(
            String specialization, Integer minExperience, Double maxFee) {
        return Specification
                .where(hasSpecialization(specialization))
                .and(hasMinExperience(minExperience))
                .and(hasMaxFee(maxFee));
    }
}