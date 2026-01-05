package example.backend.services.implementations;

import example.backend.exceptions.EntityNotFoundException;
import example.backend.exceptions.ImpossibleOperationException;
import example.backend.models.User;
import example.backend.repositories.UserRepository;
import example.backend.services.protocols.EmailService;
import example.backend.services.protocols.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public void createAndSendVerification(User user) {
        String code = generateCode();
        user.setVerificationCode(code);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), code);
    }

    @Override
    public void resendVerification(User user) {

    }

    @Override
    @Transactional
    public void verify(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User", "email", email));

        if (user.isVerified()) {
            throw new ImpossibleOperationException("User already verified!");
        }

        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ImpossibleOperationException("Verification code expired!");
        }

        if (!user.getVerificationCode().equals(code)) {
            throw new ImpossibleOperationException("Verification code does not match!");
        }

        user.setVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);

        userRepository.save(user);
    }

    private String generateCode() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100_000, 1_000_000));
    }
}
