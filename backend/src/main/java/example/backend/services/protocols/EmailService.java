package example.backend.services.protocols;

public interface EmailService {

    void sendVerificationEmail(String to, String code);

}
