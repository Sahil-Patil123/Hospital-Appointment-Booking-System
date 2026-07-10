package com.sahil.hospital_appointment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahil.hospital_appointment.dto.request.AppointmentRequest;
import com.sahil.hospital_appointment.dto.response.AppointmentResponse;
import com.sahil.hospital_appointment.model.AppointmentStatus;
import com.sahil.hospital_appointment.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.sahil.hospital_appointment.security.JwtAuthenticationFilter;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.Import;
import com.sahil.hospital_appointment.config.TestSecurityConfig;

@WebMvcTest(
    controllers = AppointmentController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
@Import(TestSecurityConfig.class)
@EnableMethodSecurity
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @MockitoBean(name = "appointmentService")
    private AppointmentService appointmentService;

    @Test
    void bookAppointment_shouldReturn201_whenBookingOwnAppointment() throws Exception {
        AppointmentRequest request = new AppointmentRequest(1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0), "Checkup");

        AppointmentResponse response = AppointmentResponse.builder()
                .id(1L)
                .status(AppointmentStatus.PENDING)
                .build();

        when(appointmentService.isPatientSelf(eq(1L), eq("patient@test.com"))).thenReturn(true);
        when(appointmentService.bookAppointment(eq(1L), any(AppointmentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/appointments/patients/1")
                        .with(user("patient@test.com").roles("PATIENT"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void bookAppointment_shouldReturn403_whenBookingForSomeoneElse() throws Exception {
        AppointmentRequest request = new AppointmentRequest(1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0), "Checkup");

        // This user is NOT the owner of patientId=1
        when(appointmentService.isPatientSelf(eq(1L), eq("otherpatient@test.com"))).thenReturn(false);

        mockMvc.perform(post("/api/appointments/patients/1")
                        .with(user("otherpatient@test.com").roles("PATIENT"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void bookAppointment_shouldReturn400_whenDateIsInThePast() throws Exception {
        AppointmentRequest request = new AppointmentRequest(1L, LocalDate.of(2020, 1, 1), LocalTime.of(10, 0), "Checkup");

        when(appointmentService.isPatientSelf(eq(1L), eq("user"))).thenReturn(true);

        mockMvc.perform(post("/api/appointments/patients/1")
                        .with(user("user").roles("PATIENT"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}