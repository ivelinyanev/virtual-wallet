package example.backend.mappers;

import example.backend.dtos.user.PublicUserDto;
import example.backend.dtos.user.RegisterUserDto;
import example.backend.dtos.user.PrivateUserDto;
import example.backend.dtos.user.UpdateUserDto;
import example.backend.models.Role;
import example.backend.models.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {
    public User toUser(RegisterUserDto registerUserDto) {
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

    public User toUser(UpdateUserDto updateUserDto) {
        User user = new User();

        user.setPassword(updateUserDto.password());
        user.setEmail(updateUserDto.email());
        user.setPhoneNumber(updateUserDto.phoneNumber());
        user.setPhotoUrl(updateUserDto.photoUrl());

        return user;
    }

    public PrivateUserDto toPrivateUserDto(User user) {

        return new PrivateUserDto(
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
                        .collect(Collectors.toSet())
        );
    }

    public PublicUserDto toPublicUserDto(User user) {
        return new PublicUserDto(
                user.getId(),
                user.getUsername(),
                user.getPhotoUrl()
        );
    }
}
