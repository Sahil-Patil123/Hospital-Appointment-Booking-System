package com.sahil.hospital_appointment.dto.response;

import com.sahil.hospital_appointment.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private Long userId;
    private String fullName;
    private String email;
    private Role role;
    private String token;
    private String tokenType;
}
