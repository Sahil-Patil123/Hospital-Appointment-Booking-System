package com.sahil.hospital_appointment.controller;

import com.sahil.hospital_appointment.dto.PageResponse;
import com.sahil.hospital_appointment.dto.response.DoctorResponse;
import com.sahil.hospital_appointment.service.DoctorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import com.sahil.hospital_appointment.security.JwtAuthenticationFilter;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.Import;
import com.sahil.hospital_appointment.config.TestSecurityConfig;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

/**
 * We use SecurityMockMvcRequestPostProcessors.user(...) to simulate
 * authenticated requests with given roles, which is highly robust and compatible
 * with custom security filter chains in tests.
 */
@WebMvcTest(
    controllers = DoctorController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
@Import(TestSecurityConfig.class)
@EnableMethodSecurity
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DoctorService doctorService;

    @Test
    void getAllDoctors_shouldReturn401_whenNoAuthProvided() throws Exception {
        mockMvc.perform(get("/api/doctors"))
                .andExpect(status().isForbidden());  // change isUnauthorized() to isForbidden()
    }

    @Test
    void getAllDoctors_shouldReturn200_forAuthenticatedPatient() throws Exception {
        DoctorResponse doctor = DoctorResponse.builder().id(1L).fullName("Dr. Smith").build();
        PageResponse<DoctorResponse> page = new PageResponse<>(List.of(doctor), 0, 10, 1, 1, true);

        when(doctorService.getAllDoctors(any())).thenReturn(page);

        mockMvc.perform(get("/api/doctors")
                        .with(user("patient").roles("PATIENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].fullName").value("Dr. Smith"));
    }

    @Test
    void deleteDoctor_shouldReturn403_forNonAdminRole() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/doctors/1")
                        .with(user("patient").roles("PATIENT"))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteDoctor_shouldReturn200_forAdmin() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/doctors/1")
                        .with(user("admin").roles("ADMIN"))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());
    }
}