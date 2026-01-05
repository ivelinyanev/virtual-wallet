package example.backend.dtos.user;

import example.backend.models.Role;

import java.time.LocalDateTime;
import java.util.Set;

public record ResponseUserDto(
        Long id,
        String firstName,
        String lastName,
        String username,
        String email,
        String phoneNumber,
        String photoUrl,
        LocalDateTime createdAt,
        Set<String> roles
) {
}
