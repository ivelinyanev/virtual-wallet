package example.backend.services.implementations;

import example.backend.services.protocols.EmailService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("test")
@Service
public class DummyEmailService implements EmailService {

    @Override
    public void sendVerificationEmail(String to, String code) {
        System.out.println("[TEST EMAIL] To: " + to + " | Code: " + code);
    }
}
