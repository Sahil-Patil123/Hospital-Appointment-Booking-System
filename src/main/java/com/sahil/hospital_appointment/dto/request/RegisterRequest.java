package com.sahil.hospital_appointment.dto.request;

import com.sahil.hospital_appointment.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    // ---- Doctor-only fields (required only if role == DOCTOR) ----
    private String specialization;
    private String qualification;
    private Integer yearsOfExperience;
    private Double consultationFee;

    // ---- Patient-only fields (required only if role == PATIENT) ----
    private String dateOfBirth; // parsed to LocalDate in the Service layer
    private String gender;
    private String address;
    private String medicalHistory;

    // ---- Shared ----
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
}
