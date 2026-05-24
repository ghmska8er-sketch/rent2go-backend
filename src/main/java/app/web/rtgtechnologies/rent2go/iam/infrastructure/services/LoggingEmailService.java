package app.web.rtgtechnologies.rent2go.iam.infrastructure.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggingEmailService implements EmailService {
    private static final Logger log = LoggerFactory.getLogger(LoggingEmailService.class);

    @Override
    public void sendPasswordResetEmail(String toEmail, String token) {
        log.info("[PasswordReset] To: {} Token: {}", toEmail, token);
        try {
            java.nio.file.Files.writeString(java.nio.file.Path.of("password_reset_tokens.log"),
                    String.format("%s %s %s%n", java.time.OffsetDateTime.now(), toEmail, token),
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception e) {
            log.warn("Failed to write password reset token to file", e);
        }
    }
}
