package com.sahil.hospital_appointment.service;

import com.sahil.hospital_appointment.dto.PageResponse;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorSearchTest {

    @Mock private DoctorRepository doctorRepository;
    @Mock private DoctorMapper doctorMapper;

    @InjectMocks
    private DoctorService doctorService;

    @Test
    void searchDoctors_shouldReturnFilteredResults() {
        User user = User.builder().id(1L).fullName("Dr. Cardio").build();
        Doctor doctor = Doctor.builder().id(1L).user(user).specialization("Cardiology").build();
        DoctorResponse response = DoctorResponse.builder().id(1L).specialization("Cardiology").build();

        Pageable pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(doctor), pageable, 1);

        when(doctorRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(doctorMapper.toResponse(doctor)).thenReturn(response);

        PageResponse<DoctorResponse> result = doctorService.searchDoctors("cardio", null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSpecialization()).isEqualTo("Cardiology");
    }

    @Test
    void searchDoctors_shouldWork_whenAllFiltersNull() {
        Pageable pageable = PageRequest.of(0, 10);
        var page = new PageImpl<Doctor>(List.of(), pageable, 0);

        when(doctorRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<DoctorResponse> result = doctorService.searchDoctors(null, null, null, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }
    @Test
    void updateDoctorProfile_shouldThrowResourceNotFoundException_whenDoctorMissing() {
        DoctorProfileRequest request = new DoctorProfileRequest(
                "Neurology", "MD", 5, "9999999999", 500.0
        );
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.updateDoctorProfile(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Doctor not found with id: 99");
    }

    @Test
    void deleteDoctor_shouldThrowResourceNotFoundException_whenDoctorMissing() {
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.deleteDoctor(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(doctorRepository, never()).delete(any(Doctor.class));
    }
}