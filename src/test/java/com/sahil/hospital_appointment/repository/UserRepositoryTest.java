package com.sahil.hospital_appointment.repository;

import com.sahil.hospital_appointment.model.Role;
import com.sahil.hospital_appointment.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    private final UserRepository userRepository;

    @Autowired
    UserRepositoryTest(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Test
    @DisplayName("Should save user and find them by email successfully")
    void shouldSaveAndFindUserByEmail() {
        // Given
        User user = User.builder()
                .fullName("Test Admin")
                .email("admin@gmail.com")
                .password("a@123")
                .role(Role.ADMIN)
                .build();

        // When
        userRepository.save(user);

        // Then
        assertThat(userRepository.existsByEmail("admin@gmail.com")).isTrue();

        Optional<User> found = userRepository.findByEmail("admin@gmail.com");
        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Test Admin");
        assertThat(found.get().getRole()).isEqualTo(Role.ADMIN);
    }
}