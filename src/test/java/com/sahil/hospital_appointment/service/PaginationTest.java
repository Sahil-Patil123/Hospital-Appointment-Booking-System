package com.sahil.hospital_appointment.service;

import com.sahil.hospital_appointment.dto.PageResponse;
import com.sahil.hospital_appointment.dto.request.PatientProfileRequest;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaginationTest {

    @Mock private DoctorRepository doctorRepository;
    @Mock private DoctorMapper doctorMapper;

    @InjectMocks
    private DoctorService doctorService;

    @Test
    void getAllDoctors_shouldReturnCorrectPageMetadata() {
        User user = User.builder().id(1L).fullName("Dr. Test").build();
        Doctor doctor = Doctor.builder().id(1L).user(user).build();
        DoctorResponse response = DoctorResponse.builder().id(1L).fullName("Dr. Test").build();

        Pageable pageable = PageRequest.of(0, 10);
        var doctorPage = new PageImpl<>(List.of(doctor), pageable, 25); // 25 total, simulating more pages exist

        when(doctorRepository.findAll(pageable)).thenReturn(doctorPage);
        when(doctorMapper.toResponse(doctor)).thenReturn(response);

        PageResponse<DoctorResponse> result = doctorService.getAllDoctors(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(25);
        assertThat(result.getTotalPages()).isEqualTo(3); // 25 items / 10 per page = 3 pages
        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.isLast()).isFalse();
    }
}