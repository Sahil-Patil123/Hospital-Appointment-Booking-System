package com.sahil.hospital_appointment.service;

import com.sahil.hospital_appointment.dto.request.PatientProfileRequest;
import com.sahil.hospital_appointment.dto.response.PatientResponse;
import com.sahil.hospital_appointment.exception.ResourceNotFoundException;
import com.sahil.hospital_appointment.model.Patient;
import com.sahil.hospital_appointment.model.User;
import com.sahil.hospital_appointment.repository.PatientRepository;
import com.sahil.hospital_appointment.service.mapper.PatientMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock private PatientRepository patientRepository;
    @Mock private PatientMapper patientMapper;

    @InjectMocks
    private PatientService patientService;

    private Patient buildPatient(Long id, String email) {
        User user = User.builder().id(1L).fullName("John Patient").email(email).build();
        return Patient.builder()
                .id(id)
                .user(user)
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .gender("Male")
                .phoneNumber("9999999999")
                .address("123 Main St")
                .medicalHistory("None")
                .build();
    }

    @Test
    void getPatientById_shouldReturnPatient_whenExists() {
        Patient patient = buildPatient(1L, "patient@test.com");
        PatientResponse response = PatientResponse.builder().id(1L).fullName("John Patient").build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientMapper.toResponse(patient)).thenReturn(response);

        PatientResponse result = patientService.getPatientById(1L);

        assertThat(result.getFullName()).isEqualTo("John Patient");
    }

    @Test
    void getPatientById_shouldThrowResourceNotFoundException_whenMissing() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getPatientById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Patient not found with id: 99");
    }

    @Test
    void updatePatientProfile_shouldUpdateFieldsCorrectly() {
        Patient patient = buildPatient(1L, "patient@test.com");
        PatientProfileRequest request = new PatientProfileRequest(
                LocalDate.of(1998, 5, 10), "Female", "8888888888", "456 New St", "Diabetes"
        );

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));
        when(patientMapper.toResponse(any(Patient.class))).thenAnswer(inv -> {
            Patient p = inv.getArgument(0);
            return PatientResponse.builder()
                    .id(p.getId())
                    .gender(p.getGender())
                    .address(p.getAddress())
                    .build();
        });

        PatientResponse result = patientService.updatePatientProfile(1L, request);

        assertThat(result.getGender()).isEqualTo("Female");
        assertThat(result.getAddress()).isEqualTo("456 New St");
        verify(patientRepository).save(patient);
    }

    @Test
    void deletePatient_shouldCallRepositoryDelete_whenExists() {
        Patient patient = buildPatient(1L, "patient@test.com");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        patientService.deletePatient(1L);

        verify(patientRepository).delete(patient);
    }

    @Test
    void isOwner_shouldReturnTrue_whenEmailMatches() {
        Patient patient = buildPatient(1L, "patient@test.com");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        assertThat(patientService.isOwner(1L, "patient@test.com")).isTrue();
    }

    @Test
    void isOwner_shouldReturnFalse_whenEmailDoesNotMatch() {
        Patient patient = buildPatient(1L, "patient@test.com");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        assertThat(patientService.isOwner(1L, "someoneelse@test.com")).isFalse();
    }

    @Test
    void getAllPatients_shouldReturnPagedResults() {
        Patient patient = buildPatient(1L, "patient@test.com");
        PatientResponse response = PatientResponse.builder().id(1L).build();

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        var patientPage = new org.springframework.data.domain.PageImpl<>(List.of(patient), pageable, 1);

        when(patientRepository.findAll(pageable)).thenReturn(patientPage);
        when(patientMapper.toResponse(patient)).thenReturn(response);

        com.sahil.hospital_appointment.dto.PageResponse<PatientResponse> result =
                patientService.getAllPatients(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
     }

    @Test
    void updatePatientProfile_shouldThrowResourceNotFoundException_whenPatientMissing() {
        PatientProfileRequest request = new PatientProfileRequest(
                LocalDate.of(1990, 1, 1), "Male", "9999999999", "Some address", null
        );
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.updatePatientProfile(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Patient not found with id: 99");
    }
}