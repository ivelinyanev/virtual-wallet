package example.backend.services.protocols;

import example.backend.dtos.user.LoginUserDto;
import example.backend.dtos.user.RegisterUserDto;
import example.backend.dtos.user.VerifyUserDto;
import example.backend.models.User;

public interface AuthService {
    String login(LoginUserDto loginUserDto);

    void register(RegisterUserDto dto);

    void verifyAccount(VerifyUserDto dto);

    User getMe();
}
