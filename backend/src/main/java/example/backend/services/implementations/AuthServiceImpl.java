package example.backend.services.implementations;

import example.backend.dtos.user.*;
import example.backend.mappers.UserMapper;
import example.backend.models.User;
import example.backend.security.JwtUtils;
import example.backend.services.protocols.AuthorizationService;
import example.backend.services.protocols.AuthService;
import example.backend.services.protocols.UserService;
import example.backend.services.protocols.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

import static example.backend.utils.StringConstants.WRONG_PASSWORD;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final VerificationService verificationService;
    private final AuthorizationService authorizationService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(LoginRequest loginUserDto) {
        User user = userService.getByEmail(loginUserDto.email());

        validatePassword(loginUserDto.password(), user.getPassword());

        PrivateUserResponse userResponse = UserMapper.toPrivateUserResponse(user);

        String token = jwtUtils.generateToken(
                user.getUsername(),
                user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()));

        return new AuthResponse(token, userResponse);
    }

    @Override
    @Transactional
    public void register(RegisterRequest dto) {
        User user = UserMapper.toUser(dto);

        User savedUser = userService.registerUnverified(user);

        verificationService.createAndSendVerification(savedUser);

    }

    @Override
    public void verifyAccount(VerifyRequest dto) {
        verificationService.verify(dto.email(), dto.verificationCode());
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public User getMe() {
        return authorizationService.currentUser();
    }

    private void validatePassword(String dtoPassword, String entityPassword) {
        if (!passwordEncoder.matches(dtoPassword, entityPassword )) {
            throw new BadCredentialsException(WRONG_PASSWORD);
        }
    }
}
