package com.sahil.hospital_appointment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Replaces the raw Map<String,String> shortcut used in Step 13 - now
 * that we're wiring @Valid everywhere, every request body should be a
 * proper DTO so validation and Swagger docs (Step 21) stay consistent.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancelAppointmentRequest {

    @NotBlank(message = "Cancellation reason is required")
    private String cancellationReason;
}