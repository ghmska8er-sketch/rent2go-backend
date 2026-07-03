package app.web.rtgtechnologies.rent2go.iam.infrastructure.services;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Legacy Resend-based implementation of {@link EmailService}.
 * <p>
 * Kept in the codebase for reference/rollback only — it is NO LONGER the active
 * {@code @Primary} implementation. {@link BrevoEmailService} is now primary as of
 * the Resend-to-Brevo migration. To roll back, remove {@code @Primary} from
 * {@link BrevoEmailService} and re-add it here; no call sites need to change
 * either way since callers depend only on the {@link EmailService} interface.
 */
@Service
public class ResendEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailService.class);

    private final Resend resend;
    private final String fromEmail;

    public ResendEmailService(
            @Value("${resend.api-key}") String apiKey,
            @Value("${resend.from-email}") String fromEmail) {
        this.resend = new Resend(apiKey);
        this.fromEmail = fromEmail;
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String token) {
        String html = """
                <h2>Restablecer contraseña - Rent2Go</h2>
                <p>Usa el siguiente token para restablecer tu contraseña:</p>
                <p style="font-size:20px;font-weight:bold;letter-spacing:2px;">%s</p>
                <p>Este token expira en <strong>1 hora</strong>.</p>
                <p>Si no solicitaste esto, ignora este correo.</p>
                """.formatted(token);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(toEmail)
                .subject("Restablecer contraseña - Rent2Go")
                .html(html)
                .build();

        try {
            CreateEmailResponse response = resend.emails().send(params);
            log.info("[PasswordReset] Email sent to {} | id={}", toEmail, response.getId());
        } catch (ResendException e) {
            log.error("[PasswordReset] Failed to send email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        String html = """
                <h2>Verifica tu cuenta - Rent2Go</h2>
                <p>Usa el siguiente token para verificar tu correo electrónico:</p>
                <p style="font-size:20px;font-weight:bold;letter-spacing:2px;">%s</p>
                <p>Este token expira en <strong>24 horas</strong>.</p>
                <p>Si no creaste esta cuenta, ignora este correo.</p>
                """.formatted(token);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(toEmail)
                .subject("Verifica tu cuenta - Rent2Go")
                .html(html)
                .build();

        try {
            CreateEmailResponse response = resend.emails().send(params);
            log.info("[EmailVerification] Email sent to {} | id={}", toEmail, response.getId());
        } catch (ResendException e) {
            log.error("[EmailVerification] Failed to send email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}
