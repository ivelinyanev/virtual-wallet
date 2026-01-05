package example.backend.services.protocols;

import example.backend.models.User;

public interface VerificationService {

    void createAndSendVerification(User user);

    void resendVerification(User user);

    void verify(String email, String code);

}
