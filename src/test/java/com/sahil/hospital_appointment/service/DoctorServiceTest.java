package com.sahil.hospital_appointment.service;

import com.sahil.hospital_appointment.dto.request.DoctorProfileRequest;
import com.sahil.hospital_appointment.dto.response.DoctorResponse;
import com.sahil.hospital_appointment.exception.ResourceNotFoundException;
import com.sahil.hospital_appointment.model.Doctor;
import com.sahil.hospital_appointment.model.User;
import com.sahil.hospital_appointment.repository.DoctorRepository;
import com.sahil.hospital_appointment.service.mapper.DoctorMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock private DoctorRepository doctorRepository;
    @Mock private DoctorMapper doctorMapper;

    @InjectMocks
    private DoctorService doctorService;

    private Doctor buildDoctor(Long id, String email) {
        User user = User.builder().id(1L).fullName("Dr. Test").email(email).build();
        return Doctor.builder()
                .id(id)
                .user(user)
                .specialization("Cardiology")
                .qualification("MD")
                .yearsOfExperience(5)
                .phoneNumber("9999999999")
                .consultationFee(400.0)
                .build();
    }

    @Test
    void getDoctorById_shouldReturnDoctor_whenExists() {
        Doctor doctor = buildDoctor(1L, "doc@test.com");
        DoctorResponse response = DoctorResponse.builder().id(1L).fullName("Dr. Test").build();

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorMapper.toResponse(doctor)).thenReturn(response);

        DoctorResponse result = doctorService.getDoctorById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFullName()).isEqualTo("Dr. Test");
    }

    @Test
    void getDoctorById_shouldThrowResourceNotFoundException_whenMissing() {
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.getDoctorById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Doctor not found with id: 99");
    }

    @Test
    void updateDoctorProfile_shouldUpdateFieldsCorrectly() {
        Doctor doctor = buildDoctor(1L, "doc@test.com");
        DoctorProfileRequest request = new DoctorProfileRequest(
                "Neurology", "MBBS, MD", 10, "8888888888", 600.0
        );

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(inv -> inv.getArgument(0));
        when(doctorMapper.toResponse(any(Doctor.class))).thenAnswer(inv -> {
            Doctor d = inv.getArgument(0);
            return DoctorResponse.builder()
                    .id(d.getId())
                    .specialization(d.getSpecialization())
                    .consultationFee(d.getConsultationFee())
                    .build();
        });

        DoctorResponse result = doctorService.updateDoctorProfile(1L, request);

        assertThat(result.getSpecialization()).isEqualTo("Neurology");
        assertThat(result.getConsultationFee()).isEqualTo(600.0);
        verify(doctorRepository).save(doctor);
    }

    @Test
    void deleteDoctor_shouldCallRepositoryDelete_whenExists() {
        Doctor doctor = buildDoctor(1L, "doc@test.com");
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        doctorService.deleteDoctor(1L);

        verify(doctorRepository).delete(doctor);
    }

    @Test
    void isOwner_shouldReturnTrue_whenEmailMatches() {
        Doctor doctor = buildDoctor(1L, "doc@test.com");
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        boolean result = doctorService.isOwner(1L, "doc@test.com");

        assertThat(result).isTrue();
    }

    @Test
    void isOwner_shouldReturnFalse_whenEmailDoesNotMatch() {
        Doctor doctor = buildDoctor(1L, "doc@test.com");
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        boolean result = doctorService.isOwner(1L, "someoneelse@test.com");

        assertThat(result).isFalse();
    }

    @Test
    void getAllDoctors_shouldReturnMappedList() {
        Doctor doctor = buildDoctor(1L, "doc@test.com");
        DoctorResponse response = DoctorResponse.builder().id(1L).build();

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        var doctorPage = new org.springframework.data.domain.PageImpl<>(List.of(doctor), pageable, 1);

        when(doctorRepository.findAll(pageable)).thenReturn(doctorPage);
        when(doctorMapper.toResponse(doctor)).thenReturn(response);

        com.sahil.hospital_appointment.dto.PageResponse<DoctorResponse> results =
                doctorService.getAllDoctors(pageable);

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getId()).isEqualTo(1L);
    }
}