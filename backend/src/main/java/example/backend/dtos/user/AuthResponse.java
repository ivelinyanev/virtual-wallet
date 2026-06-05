package example.backend.dtos.user;

public record AuthResponse (
        String token,
        PrivateUserResponse user
){
}
