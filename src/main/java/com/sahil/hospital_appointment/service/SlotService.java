package com.sahil.hospital_appointment.service;

import com.sahil.hospital_appointment.dto.response.SlotResponse;
import com.sahil.hospital_appointment.exception.InvalidRequestException;
import com.sahil.hospital_appointment.exception.SlotUnavailableException;
import com.sahil.hospital_appointment.model.Appointment;
import com.sahil.hospital_appointment.model.AppointmentStatus;
import com.sahil.hospital_appointment.model.DoctorSchedule;
import com.sahil.hospital_appointment.repository.AppointmentRepository;
import com.sahil.hospital_appointment.repository.DoctorScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * THE core business logic of the app: computes which exact time slots
 * are bookable for a doctor on a given date, by combining their weekly
 * DoctorSchedule windows with existing Appointment bookings.
 *
 * Both the public "show available slots" endpoint AND the internal
 * "is this slot actually bookable" check (used by AppointmentService in
 * Step 12) go through THIS class - one source of truth, so the slots
 * shown to a patient are guaranteed to match what booking will accept.
 */
@Service
@RequiredArgsConstructor
public class SlotService {

    private final DoctorScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorService doctorService;

    @Transactional(readOnly = true)
    public SlotResponse getAvailableSlots(Long doctorId, LocalDate date) {
        doctorService.findDoctorOrThrow(doctorId); // 404 if doctor doesn't exist

        if (date.isBefore(LocalDate.now())) {
            throw new InvalidRequestException("Cannot fetch slots for a past date");
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();

        // Step 1: find this doctor's active working-hours windows for
        // this day of the week (e.g. all "MONDAY" schedule rows)
        List<DoctorSchedule> schedules =
                scheduleRepository.findByDoctorIdAndDayOfWeekAndActiveTrue(doctorId, dayOfWeek);

        if (schedules.isEmpty()) {
            // Doctor simply doesn't work on this day - empty list, not an error
            return SlotResponse.builder()
                    .doctorId(doctorId)
                    .date(date)
                    .availableSlots(List.of())
                    .build();
        }

        // Step 2: gather times already booked on this date (excluding
        // CANCELLED appointments - a cancelled slot frees back up)
        Set<LocalTime> bookedTimes = appointmentRepository
                .findByDoctorIdAndAppointmentDate(doctorId, date)
                .stream()
                .filter(appointment -> appointment.getStatus() != AppointmentStatus.CANCELLED)
                .map(Appointment::getAppointmentTime)
                .collect(Collectors.toSet());

        boolean isToday = date.isEqual(LocalDate.now());
        LocalTime now = LocalTime.now();

        List<LocalTime> availableSlots = new ArrayList<>();
        Integer slotDuration = null;

        // Step 3: slice each schedule window into fixed-size slots
        for (DoctorSchedule schedule : schedules) {
            slotDuration = schedule.getSlotDurationMinutes(); // assumes one duration per doctor for simplicity
            LocalTime slotStart = schedule.getStartTime();

            while (slotStart.plusMinutes(schedule.getSlotDurationMinutes()).compareTo(schedule.getEndTime()) <= 0) {
                boolean alreadyBooked = bookedTimes.contains(slotStart);
                boolean isPastTime = isToday && slotStart.isBefore(now);

                if (!alreadyBooked && !isPastTime) {
                    availableSlots.add(slotStart);
                }

                slotStart = slotStart.plusMinutes(schedule.getSlotDurationMinutes());
            }
        }

        return SlotResponse.builder()
                .doctorId(doctorId)
                .date(date)
                .slotDurationMinutes(slotDuration)
                .availableSlots(availableSlots)
                .build();
    }

    /**
     * Called by AppointmentService (Step 12) right before saving a new
     * booking. Reuses getAvailableSlots() so the exact same rules apply -
     * if it's not in the available list, it's not bookable, full stop.
     */
    @Transactional(readOnly = true)
    public void validateSlotAvailability(Long doctorId, LocalDate date, LocalTime time) {
        SlotResponse slots = getAvailableSlots(doctorId, date);

        if (!slots.getAvailableSlots().contains(time)) {
            throw new SlotUnavailableException(
                    "The selected time slot (" + time + " on " + date + ") is not available for this doctor");
        }
    }
}