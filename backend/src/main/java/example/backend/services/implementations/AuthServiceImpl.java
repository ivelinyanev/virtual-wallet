package example.backend.services.implementations;

import example.backend.dtos.user.RegisterUserDto;
import example.backend.dtos.user.VerifyUserDto;
import example.backend.models.User;
import example.backend.services.protocols.AuthService;
import example.backend.services.protocols.UserService;
import example.backend.services.protocols.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final VerificationService verificationService;

    @Override
    @Transactional
    public User register(RegisterUserDto dto) {
        User user = new User();

        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setPhoneNumber(dto.phoneNumber());
        user.setPassword(dto.password());

        User savedUser = userService.registerUnverified(user);

        verificationService.createAndSendVerification(savedUser);

        return savedUser;
    }

    @Override
    public void verifyAccount(VerifyUserDto dto) {
        verificationService.verify(dto.email(), dto.code());
    }
}
