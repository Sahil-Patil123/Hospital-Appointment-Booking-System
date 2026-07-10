package com.sahil.hospital_appointment.service;

import com.sahil.hospital_appointment.dto.request.RegisterRequest;
import com.sahil.hospital_appointment.dto.response.AuthResponse;
import com.sahil.hospital_appointment.exception.DuplicateResourceException;
import com.sahil.hospital_appointment.exception.InvalidRequestException;
import com.sahil.hospital_appointment.model.Role;
import com.sahil.hospital_appointment.model.User;
import com.sahil.hospital_appointment.repository.DoctorRepository;
import com.sahil.hospital_appointment.repository.PatientRepository;
import com.sahil.hospital_appointment.repository.UserRepository;
import com.sahil.hospital_appointment.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Pure unit test - all dependencies are mocked, so this runs in
 * milliseconds with NO real database or Spring context involved.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldThrowDuplicateResourceException_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@test.com");
        request.setRole(Role.PATIENT);

        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void register_shouldThrowInvalidRequestException_whenDoctorFieldsMissing() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("doc@test.com");
        request.setFullName("Dr. Test");
        request.setPassword("password123");
        request.setRole(Role.DOCTOR);
        // specialization, qualification, yearsOfExperience, consultationFee left null on purpose

        when(userRepository.existsByEmail("doc@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("required for DOCTOR registration");
    }

    @Test
    void register_shouldSucceed_forValidPatientRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("patient@test.com");
        request.setFullName("John Patient");
        request.setPassword("password123");
        request.setRole(Role.PATIENT);
        request.setDateOfBirth("2000-01-15");
        request.setGender("Male");
        request.setAddress("123 Main St");
        request.setPhoneNumber("9999999999");

        when(userRepository.existsByEmail("patient@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtUtil.generateToken(any())).thenReturn("fake-jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("patient@test.com");
        assertThat(response.getRole()).isEqualTo(Role.PATIENT);
        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }
}