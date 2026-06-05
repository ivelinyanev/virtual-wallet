package example.backend.mappers;

import example.backend.dtos.user.PublicUserResponse;
import example.backend.dtos.user.RegisterRequest;
import example.backend.dtos.user.PrivateUserResponse;
import example.backend.dtos.user.UpdateUserRequest;
import example.backend.models.Role;
import example.backend.models.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

public class UserMapper {
    public static User toUser(RegisterRequest registerUserDto) {
        User user = new User();

        user.setFirstName(registerUserDto.firstName());
        user.setLastName(registerUserDto.lastName());
        user.setUsername(registerUserDto.username());
        user.setPassword(registerUserDto.password());
        user.setEmail(registerUserDto.email());
        user.setPhoneNumber(registerUserDto.phoneNumber());

        if (registerUserDto.photoUrl() != null) {
            user.setPhotoUrl(registerUserDto.photoUrl());
        }

        return user;
    }

    public static User toUser(UpdateUserRequest updateUserDto) {
        User user = new User();

        user.setPassword(updateUserDto.password());
        user.setEmail(updateUserDto.email());
        user.setPhoneNumber(updateUserDto.phoneNumber());
        user.setPhotoUrl(updateUserDto.photoUrl());

        return user;
    }

    public static PrivateUserResponse toPrivateUserResponse(User user) {

        return new PrivateUserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getPhotoUrl(),
                user.getCreatedAt(),
                user.getRoles()
                        .stream()
                        .map(Role::getName)
                        .map(Enum::toString)
                        .collect(Collectors.toSet()),
                user.isVerified()
        );
    }

    public static PublicUserResponse toPublicUserResponse(User user) {
        return new PublicUserResponse(
                user.getId(),
                user.getUsername(),
                user.getPhotoUrl()
        );
    }
}
