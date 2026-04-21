package example.backend.dtos.user;

public record AuthResponseDto (
        String token,
        PrivateUserDto user
){
}
