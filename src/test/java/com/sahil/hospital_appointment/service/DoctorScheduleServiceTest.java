package com.sahil.hospital_appointment.service;

import com.sahil.hospital_appointment.dto.request.DoctorScheduleRequest;
import com.sahil.hospital_appointment.dto.response.DoctorScheduleResponse;
import com.sahil.hospital_appointment.exception.InvalidRequestException;
import com.sahil.hospital_appointment.exception.ResourceNotFoundException;
import com.sahil.hospital_appointment.model.Doctor;
import com.sahil.hospital_appointment.model.DoctorSchedule;
import com.sahil.hospital_appointment.model.User;
import com.sahil.hospital_appointment.repository.DoctorScheduleRepository;
import com.sahil.hospital_appointment.service.mapper.ScheduleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorScheduleServiceTest {

    @Mock private DoctorScheduleRepository scheduleRepository;
    @Mock private ScheduleMapper scheduleMapper;
    @Mock private DoctorService doctorService;

    @InjectMocks
    private DoctorScheduleService scheduleService;

    private Doctor buildDoctor(Long id, String email) {
        User user = User.builder().id(1L).email(email).build();
        return Doctor.builder().id(id).user(user).build();
    }

    private DoctorSchedule buildSchedule(Long id, Doctor doctor) {
        return DoctorSchedule.builder()
                .id(id)
                .doctor(doctor)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .slotDurationMinutes(30)
                .active(true)
                .build();
    }

    @Test
    void createSchedule_shouldSucceed_whenTimeRangeValid() {
        Doctor doctor = buildDoctor(1L, "doc@test.com");
        DoctorScheduleRequest request = new DoctorScheduleRequest(
                DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), 30
        );

        when(doctorService.findDoctorOrThrow(1L)).thenReturn(doctor);
        when(scheduleRepository.save(any(DoctorSchedule.class))).thenAnswer(inv -> inv.getArgument(0));
        when(scheduleMapper.toResponse(any(DoctorSchedule.class))).thenAnswer(inv -> {
            DoctorSchedule s = inv.getArgument(0);
            return DoctorScheduleResponse.builder()
                    .doctorId(doctor.getId())
                    .dayOfWeek(s.getDayOfWeek())
                    .startTime(s.getStartTime())
                    .endTime(s.getEndTime())
                    .build();
        });

        DoctorScheduleResponse result = scheduleService.createSchedule(1L, request);

        assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(result.getStartTime()).isEqualTo(LocalTime.of(9, 0));
    }

    @Test
    void createSchedule_shouldThrowInvalidRequestException_whenStartAfterEnd() {
        Doctor doctor = buildDoctor(1L, "doc@test.com");
        DoctorScheduleRequest request = new DoctorScheduleRequest(
                DayOfWeek.MONDAY, LocalTime.of(17, 0), LocalTime.of(9, 0), 30 // invalid: start after end
        );

        when(doctorService.findDoctorOrThrow(1L)).thenReturn(doctor);

        assertThatThrownBy(() -> scheduleService.createSchedule(1L, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Start time must be before end time");
    }

    @Test
    void getScheduleByDoctorId_shouldReturnMappedList() {
        Doctor doctor = buildDoctor(1L, "doc@test.com");
        DoctorSchedule schedule = buildSchedule(1L, doctor);
        DoctorScheduleResponse response = DoctorScheduleResponse.builder().id(1L).build();

        when(doctorService.findDoctorOrThrow(1L)).thenReturn(doctor);
        when(scheduleRepository.findByDoctorId(1L)).thenReturn(List.of(schedule));
        when(scheduleMapper.toResponse(schedule)).thenReturn(response);

        List<DoctorScheduleResponse> results = scheduleService.getScheduleByDoctorId(1L);

        assertThat(results).hasSize(1);
    }

    @Test
    void findScheduleOrThrow_shouldThrowResourceNotFoundException_whenMissing() {
        when(scheduleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleService.findScheduleOrThrow(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void isScheduleOwner_shouldReturnTrue_whenEmailMatches() {
        Doctor doctor = buildDoctor(1L, "doc@test.com");
        DoctorSchedule schedule = buildSchedule(1L, doctor);
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

        assertThat(scheduleService.isScheduleOwner(1L, "doc@test.com")).isTrue();
    }

    @Test
    void deleteSchedule_shouldCallRepositoryDelete_whenExists() {
        Doctor doctor = buildDoctor(1L, "doc@test.com");
        DoctorSchedule schedule = buildSchedule(1L, doctor);
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

        scheduleService.deleteSchedule(1L);

        verify(scheduleRepository).delete(schedule);
    }
}