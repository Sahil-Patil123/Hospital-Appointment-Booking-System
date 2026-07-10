package com.sahil.hospital_appointment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahil.hospital_appointment.dto.request.LoginRequest;
import com.sahil.hospital_appointment.dto.request.RegisterRequest;
import com.sahil.hospital_appointment.dto.response.AuthResponse;
import com.sahil.hospital_appointment.model.Role;
import com.sahil.hospital_appointment.security.JwtAuthenticationFilter;
import com.sahil.hospital_appointment.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.Import;
import com.sahil.hospital_appointment.config.TestSecurityConfig;

@WebMvcTest(
    controllers = AuthController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
@Import(TestSecurityConfig.class)
@EnableMethodSecurity
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @MockitoBean
    private AuthService authService;

    @Test
    void register_shouldReturn201_withValidRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("John Doe");
        request.setEmail("john@test.com");
        request.setPassword("password123");
        request.setRole(Role.PATIENT);
        request.setDateOfBirth("2000-01-01");
        request.setGender("Male");
        request.setAddress("123 Main St");
        request.setPhoneNumber("9999999999");

        AuthResponse response = AuthResponse.builder()
                .userId(1L)
                .fullName("John Doe")
                .email("john@test.com")
                .role(Role.PATIENT)
                .token("fake-jwt-token")
                .tokenType("Bearer")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("john@test.com"))
                .andExpect(jsonPath("$.data.token").value("fake-jwt-token"));
    }

    @Test
    void register_shouldReturn400_whenEmailInvalid() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("John Doe");
        request.setEmail("not-an-email");
        request.setPassword("password123");
        request.setRole(Role.PATIENT);
        request.setPhoneNumber("9999999999");

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.email").exists());
    }

    @Test
    void login_shouldReturn200_withValidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@test.com");
        request.setPassword("password123");

        AuthResponse response = AuthResponse.builder()
                .userId(1L)
                .email("john@test.com")
                .role(Role.PATIENT)
                .token("fake-jwt-token")
                .tokenType("Bearer")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("fake-jwt-token"));
    }
}