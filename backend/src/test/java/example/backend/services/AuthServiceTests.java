package example.backend.services;

import example.backend.dtos.user.LoginUserDto;
import example.backend.dtos.user.RegisterUserDto;
import example.backend.dtos.user.VerifyUserDto;
import example.backend.enums.ERole;
import example.backend.mappers.UserMapper;
import example.backend.models.Role;
import example.backend.models.User;
import example.backend.security.JwtUtils;
import example.backend.services.implementations.AuthServiceImpl;
import example.backend.services.protocols.UserService;
import example.backend.services.protocols.VerificationService;
import example.backend.utils.AuthUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTests {

    @Mock
    private UserService userService;

    @Mock
    private VerificationService verificationService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthUtils authUtils;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void login_ShouldReturnJwtToken_When_CredentialsAreValid() {
        LoginUserDto dto = new LoginUserDto("test@mail.com", "password");

        Role role = new Role();
        role.setName(ERole.ROLE_USER);

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRoles(Set.of(role));

        when(userService.getByEmail(dto.email())).thenReturn(user);
        when(passwordEncoder.matches(dto.password(), user.getPassword())).thenReturn(true);
        when(jwtUtils.generateToken(eq("testuser"), anySet())).thenReturn("jwt-token");

        String token = authService.login(dto);

        assertEquals("jwt-token", token);
        verify(jwtUtils).generateToken(eq("testuser"), anySet());
    }

    @Test
    void login_ShouldThrowException_When_PasswordIsIncorrect() {
        LoginUserDto dto = new LoginUserDto("test@mail.com", "wrong");

        User user = new User();
        user.setPassword("encodedPassword");

        when(userService.getByEmail(dto.email())).thenReturn(user);
        when(passwordEncoder.matches(dto.password(), user.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(dto));
        verify(jwtUtils, never()).generateToken(any(), any());
    }

    @Test
    void register_ShouldCreateUser_And_SendVerification() {
        RegisterUserDto dto = mock(RegisterUserDto.class);
        User mappedUser = new User();
        User savedUser = new User();

        when(userMapper.toUser(dto)).thenReturn(mappedUser);
        when(userService.registerUnverified(mappedUser)).thenReturn(savedUser);

        authService.register(dto);

        verify(userMapper).toUser(dto);
        verify(userService).registerUnverified(mappedUser);
        verify(verificationService).createAndSendVerification(savedUser);
    }

    @Test
    void verifyAccount_Should_CallVerificationService() {
        VerifyUserDto dto = new VerifyUserDto("test@mail.com", "password");

        authService.verifyAccount(dto);

        verify(verificationService).verify(dto.email(), dto.code());
    }

    @Test
    void getMe_Should_ReturnAuthenticatedUser() {
        User user = new User();

        when(authUtils.getAuthenticatedUser()).thenReturn(user);

        User result= authService.getMe();

        assertEquals(user, result);
    }
}
