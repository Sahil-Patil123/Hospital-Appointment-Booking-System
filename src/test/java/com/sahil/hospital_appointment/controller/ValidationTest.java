package com.sahil.hospital_appointment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahil.hospital_appointment.dto.request.LoginRequest;
import com.sahil.hospital_appointment.dto.request.RegisterRequest;
import com.sahil.hospital_appointment.exception.GlobalExceptionHandler;
import com.sahil.hospital_appointment.model.Role;
import com.sahil.hospital_appointment.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ValidationTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(authService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void register_shouldReturn400_whenEmailIsInvalid() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Test User");
        request.setEmail("not-an-email"); // invalid format
        request.setPassword("password123");
        request.setRole(Role.PATIENT);
        request.setPhoneNumber("9999999999");

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.email").exists());
    }

    @Test
    void register_shouldReturn400_whenPasswordTooShort() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Test User");
        request.setEmail("valid@test.com");
        request.setPassword("123"); // under 6 chars
        request.setRole(Role.PATIENT);
        request.setPhoneNumber("9999999999");

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.password").exists());
    }

    @Test
    void register_shouldReturn400_whenFullNameIsBlank() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName(""); // blank
        request.setEmail("valid@test.com");
        request.setPassword("password123");
        request.setRole(Role.PATIENT);
        request.setPhoneNumber("9999999999");

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.fullName").exists());
    }

    @Test
    void login_shouldReturn400_whenPasswordMissing() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("valid@test.com");
        request.setPassword(""); // blank

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.password").exists());
    }
}