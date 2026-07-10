package com.sahil.hospital_appointment.service;

import com.sahil.hospital_appointment.dto.request.LoginRequest;
import com.sahil.hospital_appointment.dto.request.RegisterRequest;
import com.sahil.hospital_appointment.dto.response.AuthResponse;
import com.sahil.hospital_appointment.exception.DuplicateResourceException;
import com.sahil.hospital_appointment.exception.InvalidRequestException;
import com.sahil.hospital_appointment.model.Doctor;
import com.sahil.hospital_appointment.model.Patient;
import com.sahil.hospital_appointment.model.Role;
import com.sahil.hospital_appointment.model.User;
import com.sahil.hospital_appointment.repository.DoctorRepository;
import com.sahil.hospital_appointment.repository.PatientRepository;
import com.sahil.hospital_appointment.repository.UserRepository;
import com.sahil.hospital_appointment.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new User, and - depending on role - also creates the
     * linked Doctor or Patient profile row. Returns a JWT immediately so
     * the client is logged in right after signup, no separate call needed.
     */
    @Transactional // if the Doctor/Patient save fails, the User save rolls back too
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already registered: " + request.getEmail());
        }

        // Build and save the base User record - password is hashed here,
        // NEVER stored as plain text
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(true)
                .build();
        user = userRepository.save(user);

        // Role-specific profile creation
        if (request.getRole() == Role.DOCTOR) {
            createDoctorProfile(request, user);
        } else if (request.getRole() == Role.PATIENT) {
            createPatientProfile(request, user);
        }
        // ADMIN needs no extra profile row - the User record is sufficient

        return buildAuthResponse(user);
    }

    /**
     * Authenticates email+password via Spring Security's AuthenticationManager
     * (wired in Step 6). If credentials are wrong, this throws
     * BadCredentialsException automatically - already handled by
     * GlobalExceptionHandler (Step 5), so no try/catch needed here.
     */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found in database - this should never happen"));

        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .token(token)
                .tokenType("Bearer")
                .build();
    }

    // ---------- Private helpers ----------

    private void createDoctorProfile(RegisterRequest request, User user) {
        if (request.getSpecialization() == null || request.getSpecialization().isBlank()
                || request.getQualification() == null || request.getQualification().isBlank()
                || request.getYearsOfExperience() == null
                || request.getConsultationFee() == null) {
            throw new InvalidRequestException(
                    "Specialization, qualification, yearsOfExperience, and consultationFee are required for DOCTOR registration");
        }

        Doctor doctor = Doctor.builder()
                .user(user)
                .specialization(request.getSpecialization())
                .qualification(request.getQualification())
                .yearsOfExperience(request.getYearsOfExperience())
                .phoneNumber(request.getPhoneNumber())
                .consultationFee(request.getConsultationFee())
                .build();

        doctorRepository.save(doctor);
    }

    private void createPatientProfile(RegisterRequest request, User user) {
        if (request.getDateOfBirth() == null || request.getDateOfBirth().isBlank()
                || request.getGender() == null || request.getGender().isBlank()
                || request.getAddress() == null || request.getAddress().isBlank()) {
            throw new InvalidRequestException(
                    "dateOfBirth, gender, and address are required for PATIENT registration");
        }

        LocalDate dateOfBirth;
        try {
            dateOfBirth = LocalDate.parse(request.getDateOfBirth()); // expects "YYYY-MM-DD"
        } catch (DateTimeParseException ex) {
            throw new InvalidRequestException("dateOfBirth must be in YYYY-MM-DD format");
        }

        Patient patient = Patient.builder()
                .user(user)
                .dateOfBirth(dateOfBirth)
                .gender(request.getGender())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .medicalHistory(request.getMedicalHistory())
                .build();

        patientRepository.save(patient);
    }

    // Builds the JWT + response DTO for a freshly registered user
    private AuthResponse buildAuthResponse(User user) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build();

        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .token(token)
                .tokenType("Bearer")
                .build();
    }
}