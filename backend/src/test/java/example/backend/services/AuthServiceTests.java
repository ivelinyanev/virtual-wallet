package example.backend.services;

import example.backend.dtos.user.AuthResponse;
import example.backend.dtos.user.LoginRequest;
import example.backend.dtos.user.RegisterRequest;
import example.backend.dtos.user.VerifyRequest;
import example.backend.enums.ERole;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTests {

    @Mock private UserService userService;
    @Mock private VerificationService verificationService;
    @Mock private AuthUtils authUtils;
    @Mock private JwtUtils jwtUtils;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void login_Should_ReturnAuthResponse_When_CredentialsAreValid() {
        LoginRequest dto = new LoginRequest("test@mail.com", "password");

        Role role = new Role();
        role.setName(ERole.ROLE_USER);

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setRoles(Set.of(role));

        when(userService.getByEmail(dto.email())).thenReturn(user);
        when(passwordEncoder.matches(dto.password(), user.getPassword())).thenReturn(true);
        when(jwtUtils.generateToken(eq("testuser"), anySet())).thenReturn("jwt-token");

        AuthResponse response = authService.login(dto);

        assertEquals("jwt-token", response.token());
        verify(jwtUtils).generateToken(eq("testuser"), anySet());
    }

    @Test
    void login_Should_ThrowBadCredentials_When_PasswordIsIncorrect() {
        LoginRequest dto = new LoginRequest("test@mail.com", "wrong");

        User user = new User();
        user.setPassword("encodedPassword");

        when(userService.getByEmail(dto.email())).thenReturn(user);
        when(passwordEncoder.matches(dto.password(), user.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(dto));
        verify(jwtUtils, never()).generateToken(any(), any());
    }

    @Test
    void register_Should_CreateUser_And_SendVerification() {
        RegisterRequest dto = mock(RegisterRequest.class);
        User savedUser = new User();

        when(userService.registerUnverified(any(User.class))).thenReturn(savedUser);

        authService.register(dto);

        verify(userService).registerUnverified(any(User.class));
        verify(verificationService).createAndSendVerification(savedUser);
    }

    @Test
    void verifyAccount_Should_DelegateToVerificationService() {
        VerifyRequest dto = new VerifyRequest("test@mail.com", "123456");

        authService.verifyAccount(dto);

        verify(verificationService).verify(dto.email(), dto.verificationCode());
    }

    @Test
    void getMe_Should_ReturnAuthenticatedUser() {
        User user = new User();
        when(authUtils.getAuthenticatedUser()).thenReturn(user);

        User result = authService.getMe();

        assertEquals(user, result);
    }
}
