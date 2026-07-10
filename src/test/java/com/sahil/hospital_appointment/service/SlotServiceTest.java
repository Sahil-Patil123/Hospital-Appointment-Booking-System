package com.sahil.hospital_appointment.service;

import com.sahil.hospital_appointment.dto.response.SlotResponse;
import com.sahil.hospital_appointment.exception.SlotUnavailableException;
import com.sahil.hospital_appointment.model.*;
import com.sahil.hospital_appointment.repository.AppointmentRepository;
import com.sahil.hospital_appointment.repository.DoctorScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlotServiceTest {

    @Mock private DoctorScheduleRepository scheduleRepository;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private DoctorService doctorService;

    @InjectMocks
    private SlotService slotService;

    // A fixed future Monday so isToday-based logic never interferes with this test
    private LocalDate nextMonday() {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek() != DayOfWeek.MONDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    private DoctorSchedule buildSchedule(LocalTime start, LocalTime end, int duration) {
        Doctor doctor = Doctor.builder().id(1L).build();
        return DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(start)
                .endTime(end)
                .slotDurationMinutes(duration)
                .active(true)
                .build();
    }

    @Test
    void getAvailableSlots_shouldReturnEmptyList_whenNoScheduleForThatDay() {
        LocalDate date = nextMonday();
        when(scheduleRepository.findByDoctorIdAndDayOfWeekAndActiveTrue(1L, DayOfWeek.MONDAY))
                .thenReturn(List.of());

        SlotResponse result = slotService.getAvailableSlots(1L, date);

        assertThat(result.getAvailableSlots()).isEmpty();
    }

    @Test
    void getAvailableSlots_shouldGenerateCorrectSlots_forSimpleWindow() {
        LocalDate date = nextMonday();
        DoctorSchedule schedule = buildSchedule(LocalTime.of(9, 0), LocalTime.of(11, 0), 30);

        when(scheduleRepository.findByDoctorIdAndDayOfWeekAndActiveTrue(1L, DayOfWeek.MONDAY))
                .thenReturn(List.of(schedule));
        when(appointmentRepository.findByDoctorIdAndAppointmentDate(1L, date))
                .thenReturn(List.of());

        SlotResponse result = slotService.getAvailableSlots(1L, date);

        // 9:00-11:00 in 30-min slots = 9:00, 9:30, 10:00, 10:30 (4 slots -
        // 10:30+30min = 11:00 which is NOT <= 11:00 exclusive of the boundary edge...
        // actually 10:30+30=11:00 which IS <= 11:00, so it's included)
        assertThat(result.getAvailableSlots()).containsExactly(
                LocalTime.of(9, 0), LocalTime.of(9, 30),
                LocalTime.of(10, 0), LocalTime.of(10, 30)
        );
    }

    @Test
    void getAvailableSlots_shouldExcludeAlreadyBookedSlot() {
        LocalDate date = nextMonday();
        DoctorSchedule schedule = buildSchedule(LocalTime.of(9, 0), LocalTime.of(10, 0), 30);

        Appointment bookedAppointment = Appointment.builder()
                .appointmentTime(LocalTime.of(9, 30))
                .status(AppointmentStatus.CONFIRMED)
                .build();

        when(scheduleRepository.findByDoctorIdAndDayOfWeekAndActiveTrue(1L, DayOfWeek.MONDAY))
                .thenReturn(List.of(schedule));
        when(appointmentRepository.findByDoctorIdAndAppointmentDate(1L, date))
                .thenReturn(List.of(bookedAppointment));

        SlotResponse result = slotService.getAvailableSlots(1L, date);

        assertThat(result.getAvailableSlots()).containsExactly(LocalTime.of(9, 0));
    }

    @Test
    void getAvailableSlots_shouldIncludeSlot_whenExistingAppointmentIsCancelled() {
        LocalDate date = nextMonday();
        DoctorSchedule schedule = buildSchedule(LocalTime.of(9, 0), LocalTime.of(10, 0), 30);

        Appointment cancelledAppointment = Appointment.builder()
                .appointmentTime(LocalTime.of(9, 30))
                .status(AppointmentStatus.CANCELLED) // cancelled slot frees back up
                .build();

        when(scheduleRepository.findByDoctorIdAndDayOfWeekAndActiveTrue(1L, DayOfWeek.MONDAY))
                .thenReturn(List.of(schedule));
        when(appointmentRepository.findByDoctorIdAndAppointmentDate(1L, date))
                .thenReturn(List.of(cancelledAppointment));

        SlotResponse result = slotService.getAvailableSlots(1L, date);

        assertThat(result.getAvailableSlots()).contains(LocalTime.of(9, 30));
    }

    @Test
    void validateSlotAvailability_shouldThrowSlotUnavailableException_whenSlotNotFree() {
        LocalDate date = nextMonday();
        DoctorSchedule schedule = buildSchedule(LocalTime.of(9, 0), LocalTime.of(10, 0), 30);

        Appointment bookedAppointment = Appointment.builder()
                .appointmentTime(LocalTime.of(9, 0))
                .status(AppointmentStatus.CONFIRMED)
                .build();

        when(scheduleRepository.findByDoctorIdAndDayOfWeekAndActiveTrue(1L, DayOfWeek.MONDAY))
                .thenReturn(List.of(schedule));
        when(appointmentRepository.findByDoctorIdAndAppointmentDate(1L, date))
                .thenReturn(List.of(bookedAppointment));

        assertThatThrownBy(() -> slotService.validateSlotAvailability(1L, date, LocalTime.of(9, 0)))
                .isInstanceOf(SlotUnavailableException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void validateSlotAvailability_shouldPass_whenSlotIsFree() {
        LocalDate date = nextMonday();
        DoctorSchedule schedule = buildSchedule(LocalTime.of(9, 0), LocalTime.of(10, 0), 30);

        when(scheduleRepository.findByDoctorIdAndDayOfWeekAndActiveTrue(1L, DayOfWeek.MONDAY))
                .thenReturn(List.of(schedule));
        when(appointmentRepository.findByDoctorIdAndAppointmentDate(1L, date))
                .thenReturn(List.of());

        // Should NOT throw
        slotService.validateSlotAvailability(1L, date, LocalTime.of(9, 0));
    }
}