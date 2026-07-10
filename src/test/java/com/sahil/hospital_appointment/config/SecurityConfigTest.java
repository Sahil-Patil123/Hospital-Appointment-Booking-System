package com.sahil.hospital_appointment.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // Tells Spring to spin up your entire backend application in the background specifically for this test run
@AutoConfigureMockMvc // Automatically configures a fake, simulated browser client (MockMvc) to send HTTP requests to your app
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc; // Injects the simulated browser tool so you can mimic real API network calls (GET, POST, etc.)

    @Test // Marks this method as an automated test case that Maven/Gradle or your IDE can execute
    void unauthenticatedRequestToProtectedRoute_shouldReturn401or403() throws Exception {
        // 1. Pretend an unauthenticated anonymous user tries to sneak into a protected endpoint (/api/doctors)
        mockMvc.perform(get("/api/doctors"))
                // 2. Assert and verify that the firewall intercepts them and returns a 403 Forbidden status code
                .andExpect(status().isForbidden()); // If it returns 200 OK instead, the test fails, warning you that your security is broken!
    }

    @Test // Marks this as another separate test scenario
    void swaggerUiPath_shouldBePubliclyAccessible() throws Exception {
        // 1. Pretend a regular public user requests your API documentation data page
        mockMvc.perform(get("/v3/api-docs"))
                // 2. Assert and verify that the firewall recognizes it as public (permitAll) and allows it with a 200 OK status code
                .andExpect(status().isOk());
    }
}