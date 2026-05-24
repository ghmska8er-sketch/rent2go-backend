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
    }
}
