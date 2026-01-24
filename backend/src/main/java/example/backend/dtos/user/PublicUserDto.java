package example.backend.dtos.user;

public record PublicUserDto(
        Long id,
        String username,
        String photoUrl
) {
}
