package com.sahil.hospital_appointment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Response shape for GET /api/slots - tells the client exactly which
 * times are bookable for a given doctor on a given date.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotResponse {

    private Long doctorId;
    private LocalDate date;
    private Integer slotDurationMinutes;
    private List<LocalTime> availableSlots;
}