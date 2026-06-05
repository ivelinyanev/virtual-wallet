package example.backend.dtos.user;

public record PublicUserResponse(
        Long id,
        String username,
        String photoUrl
) {
}
