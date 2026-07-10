package com.sahil.hospital_appointment.controller;

import com.sahil.hospital_appointment.dto.ApiResponse;
import com.sahil.hospital_appointment.dto.response.SlotResponse;
import com.sahil.hospital_appointment.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/doctors/{doctorId}/slots")
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;

    @GetMapping
    public ResponseEntity<ApiResponse<SlotResponse>> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        SlotResponse response = slotService.getAvailableSlots(doctorId, date);
        return ResponseEntity.ok(ApiResponse.success("Available slots fetched successfully", response));
    }
}