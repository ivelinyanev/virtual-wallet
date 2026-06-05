package example.backend.services;

import example.backend.exceptions.EntityNotFoundException;
import example.backend.exceptions.ImpossibleOperationException;
import example.backend.models.User;
import example.backend.repositories.UserRepository;
import example.backend.services.implementations.VerificationServiceImpl;
import example.backend.services.protocols.EmailService;
import example.backend.services.protocols.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static example.backend.utils.StringConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VerificationServiceTests {

    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;
    @Mock private WalletService walletService;

    @InjectMocks
    private VerificationServiceImpl verificationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@mail.com");
        user.setVerified(false);
    }

    // ───────────────────────── createAndSendVerification ─────────────────────────

    @Test
    void createAndSendVerification_Should_SetCodeAndExpiry_AndSendEmail() {
        verificationService.createAndSendVerification(user);

        assertNotNull(user.getVerificationCode());
        assertNotNull(user.getVerificationCodeExpiresAt());
        // expiry should be ~15 minutes from now
        assertTrue(user.getVerificationCodeExpiresAt().isAfter(LocalDateTime.now().plusMinutes(14)));

        verify(userRepository).save(user);
        verify(emailService).sendVerificationEmail(eq("test@mail.com"), anyString());
    }

    // ───────────────────────── resendVerification ─────────────────────────

    @Test
    void resendVerification_Should_SetNewCodeAndShortExpiry_AndSendEmail() {
        verificationService.resendVerification(user);

        assertNotNull(user.getVerificationCode());
        // expiry should be ~5 minutes from now (shorter than initial 15 min)
        assertTrue(user.getVerificationCodeExpiresAt().isAfter(LocalDateTime.now().plusMinutes(4)));
        assertTrue(user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now().plusMinutes(6)));

        verify(userRepository).save(user);
        verify(emailService).sendVerificationEmail(eq("test@mail.com"), anyString());
    }

    // ───────────────────────── verify ─────────────────────────

    @Test
    void verify_Should_VerifyUser_When_CodeIsCorrectAndNotExpired() {
        user.setVerificationCode("123456");
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));

        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));

        verificationService.verify("test@mail.com", "123456");

        assertTrue(user.isVerified());
        assertNull(user.getVerificationCode());
        assertNull(user.getVerificationCodeExpiresAt());
        verify(userRepository).save(user);
        verify(walletService).createBaseWalletUponVerification(user);
    }

    @Test
    void verify_Should_Throw_When_UserNotFound() {
        when(userRepository.findByEmail("ghost@mail.com")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> verificationService.verify("ghost@mail.com", "123456"));
    }

    @Test
    void verify_Should_Throw_When_AlreadyVerified() {
        user.setVerified(true);
        user.setVerificationCode("123456");
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));

        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));

        ImpossibleOperationException ex =
                assertThrows(ImpossibleOperationException.class,
                        () -> verificationService.verify("test@mail.com", "123456"));

        assertEquals(USER_ALREADY_VERIFIED, ex.getMessage());
        verify(walletService, never()).createBaseWalletUponVerification(any());
    }

    @Test
    void verify_Should_Throw_When_CodeIsExpired() {
        user.setVerificationCode("123456");
        user.setVerificationCodeExpiresAt(LocalDateTime.now().minusMinutes(1)); // expired

        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));

        ImpossibleOperationException ex =
                assertThrows(ImpossibleOperationException.class,
                        () -> verificationService.verify("test@mail.com", "123456"));

        assertEquals(VERIFICATION_CODE_EXPIRED, ex.getMessage());
        verify(walletService, never()).createBaseWalletUponVerification(any());
    }

    @Test
    void verify_Should_Throw_When_CodeDoesNotMatch() {
        user.setVerificationCode("123456");
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));

        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));

        ImpossibleOperationException ex =
                assertThrows(ImpossibleOperationException.class,
                        () -> verificationService.verify("test@mail.com", "000000"));

        assertEquals(VERIFICATION_CODE_DOES_NOT_MATCH, ex.getMessage());
        verify(walletService, never()).createBaseWalletUponVerification(any());
    }
}
