package com.sahil.hospital_appointment.service.mapper;

import com.sahil.hospital_appointment.dto.response.DoctorScheduleResponse;
import com.sahil.hospital_appointment.model.DoctorSchedule;
import org.springframework.stereotype.Component;

/**
 * Converts DoctorSchedule entities into DoctorScheduleResponse DTOs -
 * same pattern as DoctorMapper (Step 8) and PatientMapper (Step 9).
 */
@Component
public class ScheduleMapper {

    public DoctorScheduleResponse toResponse(DoctorSchedule schedule) {
        return DoctorScheduleResponse.builder()
                .id(schedule.getId())
                .doctorId(schedule.getDoctor().getId())
                .dayOfWeek(schedule.getDayOfWeek())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .slotDurationMinutes(schedule.getSlotDurationMinutes())
                .active(schedule.isActive())
                .build();
    }
}