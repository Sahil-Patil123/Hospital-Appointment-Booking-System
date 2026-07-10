package com.sahil.hospital_appointment.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @RestController
    static class DummyController {

        @GetMapping("/test/not-found")
        public String triggerNotFound() {
            throw new ResourceNotFoundException("Doctor", 99L);
        }

        @GetMapping("/test/duplicate")
        public String triggerDuplicate() {
            throw new DuplicateResourceException("Email already registered");
        }

        @GetMapping("/test/invalid-request")
        public String triggerInvalidRequest() {
            throw new InvalidRequestException("Cannot cancel a completed appointment");
        }

        @GetMapping("/test/slot-unavailable")
        public String triggerSlotUnavailable() {
            throw new SlotUnavailableException("Slot already booked");
        }

        @GetMapping("/test/unauthorized")
        public String triggerUnauthorized() {
            throw new UnauthorizedActionException("You cannot access another patient's appointment");
        }

        @GetMapping("/test/generic-error")
        public String triggerGenericError() {
            throw new RuntimeException("Something broke unexpectedly");
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new DummyController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("ResourceNotFoundException should return 404")
    void shouldReturn404ForResourceNotFound() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Doctor not found with id: 99"));
    }

    @Test
    @DisplayName("DuplicateResourceException should return 409")
    void shouldReturn409ForDuplicateResource() throws Exception {
        mockMvc.perform(get("/test/duplicate"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email already registered"));
    }

    @Test
    @DisplayName("InvalidRequestException should return 400")
    void shouldReturn400ForInvalidRequest() throws Exception {
        mockMvc.perform(get("/test/invalid-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cannot cancel a completed appointment"));
    }

    @Test
    @DisplayName("SlotUnavailableException should return 409")
    void shouldReturn409ForSlotUnavailable() throws Exception {
        mockMvc.perform(get("/test/slot-unavailable"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Slot already booked"));
    }

    @Test
    @DisplayName("UnauthorizedActionException should return 403")
    void shouldReturn403ForUnauthorizedAction() throws Exception {
        mockMvc.perform(get("/test/unauthorized"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You cannot access another patient's appointment"));
    }

    @Test
    @DisplayName("Unhandled RuntimeException should fall back to 500")
    void shouldReturn500ForGenericException() throws Exception {
        mockMvc.perform(get("/test/generic-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }
}