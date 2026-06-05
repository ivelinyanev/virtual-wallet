package example.backend.services.protocols;

import example.backend.dtos.user.AuthResponse;
import example.backend.dtos.user.LoginRequest;
import example.backend.dtos.user.RegisterRequest;
import example.backend.dtos.user.VerifyRequest;
import example.backend.models.User;

public interface AuthService {
    AuthResponse login(LoginRequest loginUserDto);

    void register(RegisterRequest dto);

    void verifyAccount(VerifyRequest dto);

    User getMe();
}
