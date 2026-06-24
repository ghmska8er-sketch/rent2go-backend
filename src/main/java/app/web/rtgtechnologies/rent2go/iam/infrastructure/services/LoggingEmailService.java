package app.web.rtgtechnologies.rent2go.iam.infrastructure.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggingEmailService implements EmailService {
    private static final Logger log = LoggerFactory.getLogger(LoggingEmailService.class);

    @Override
    public void sendPasswordResetEmail(String toEmail, String token) {
        // Production-like behavior: log that a password reset email was requested. Do NOT persist tokens to disk.
        //log.info("[PasswordReset] To: {} (token hidden in logs for security)", toEmail);
        log.info("[PasswordReset] To: {} | Token: {}", toEmail, token);
    }
}
