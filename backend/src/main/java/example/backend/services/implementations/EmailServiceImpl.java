package example.backend.services.implementations;

import example.backend.exceptions.EmailException;
import example.backend.services.protocols.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import static example.backend.utils.StringConstants.FAILED_TO_SEND_EMAIL;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendVerificationEmail(String to, String code) {
        String subject = "Verify your account";
        String body = buildVerificationHtml(code);

        sendHtml(to, subject, body);
    }

    private void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new EmailException(FAILED_TO_SEND_EMAIL + e);
        }
    }

    private String buildVerificationHtml(String code) {
        return """
                <html>
                    <body>
                        <h2>Your verification code</h2>
                        <h1>%s</h1>
                    </body>
                </html>
                """.formatted(code);
    }
}
