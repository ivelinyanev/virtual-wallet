package example.backend.services.protocols;

import example.backend.dtos.user.LoginUserDto;
import example.backend.models.User;

public interface AuthService {
    User authenticate(LoginUserDto dto);
}
