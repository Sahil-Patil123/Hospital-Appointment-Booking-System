package com.sahil.hospital_appointment.controller;

import com.sahil.hospital_appointment.dto.ApiResponse;
import com.sahil.hospital_appointment.dto.request.DoctorScheduleRequest;
import com.sahil.hospital_appointment.dto.response.DoctorScheduleResponse;
import com.sahil.hospital_appointment.service.DoctorScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Schedule endpoints - nested under /api/doctors/{doctorId}/schedules for
 * create/list, and /api/schedules/{id} for update/delete since those
 * operate on the schedule entry directly, not through its parent doctor.
 */
@RestController
@RequiredArgsConstructor
public class DoctorScheduleController {

    private final DoctorScheduleService scheduleService;

    // Anyone authenticated can view a doctor's schedule (needed to book)
    @GetMapping("/api/doctors/{doctorId}/schedules")
    public ResponseEntity<ApiResponse<List<DoctorScheduleResponse>>> getScheduleByDoctor(
            @PathVariable Long doctorId) {
        List<DoctorScheduleResponse> schedules = scheduleService.getScheduleByDoctorId(doctorId);
        return ResponseEntity.ok(ApiResponse.success("Schedule fetched successfully", schedules));
    }

    // Only the doctor themself (or ADMIN) can add a schedule slot to
    // their own profile
    @PostMapping("/api/doctors/{doctorId}/schedules")
    @PreAuthorize("hasRole('ADMIN') or @doctorScheduleService.isDoctorSelf(#doctorId, authentication.name)")
    public ResponseEntity<ApiResponse<DoctorScheduleResponse>> createSchedule(
            @PathVariable Long doctorId,
            @Valid @RequestBody DoctorScheduleRequest request) {
        DoctorScheduleResponse created = scheduleService.createSchedule(doctorId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Schedule created successfully", created));
    }

    @PutMapping("/api/schedules/{id}")
    @PreAuthorize("hasRole('ADMIN') or @doctorScheduleService.isScheduleOwner(#id, authentication.name)")
    public ResponseEntity<ApiResponse<DoctorScheduleResponse>> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody DoctorScheduleRequest request) {
        DoctorScheduleResponse updated = scheduleService.updateSchedule(id, request);
        return ResponseEntity.ok(ApiResponse.success("Schedule updated successfully", updated));
    }

    // Only the owning doctor (or ADMIN) can delete a specific schedule entry
    @DeleteMapping("/api/schedules/{id}")
    @PreAuthorize("hasRole('ADMIN') or @doctorScheduleService.isScheduleOwner(#id, authentication.name)")
    public ResponseEntity<ApiResponse<Object>> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.ok(ApiResponse.success("Schedule deleted successfully"));
    }
}