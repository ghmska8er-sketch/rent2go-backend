package app.web.rtgtechnologies.rent2go.iam.infrastructure.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Brevo (formerly Sendinblue) transactional email implementation of {@link EmailService}.
 * <p>
 * Uses Brevo's transactional email HTTP API directly (POST /v3/smtp/email) via the
 * JDK's built-in {@link HttpClient}, following the same "no heavyweight vendor SDK"
 * approach already used by {@code CloudinaryStorageServiceImpl} in this codebase.
 * This avoids introducing an unverified/unresolved Maven dependency: the official
 * Brevo Java SDK ({@code com.sendinblue:sib-api-v3-sdk}) is a pre-jakarta-namespace
 * artifact that bundles its own OkHttp/Gson stack and risks dependency conflicts
 * with this project's Spring Boot 3 (jakarta) stack. The plain REST call needs no
 * new dependency: {@link ObjectMapper} is already transitively available via
 * spring-boot-starter-web.
 * <p>
 * Mirrors {@link ResendEmailService}'s subject lines and Spanish-language HTML body
 * copy exactly so end-user-visible emails do not change, only the delivery mechanism.
 */
@Primary
@Service
public class BrevoEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(BrevoEmailService.class);

    static final String DEFAULT_BREVO_SEND_EMAIL_URL = "https://api.brevo.com/v3/smtp/email";

    // Value Spring Boot binds when BREVO_API_KEY is not set (see application.properties
    // default). Sending against this placeholder always fails at Brevo with an auth
    // error. Per the Cloudinary incident (placeholder credentials silently failing on
    // profile-photo upload), we fail fast and log loudly here instead of letting Brevo
    // return an opaque 401 that gets swallowed.
    private static final String PLACEHOLDER_API_KEY = "brevo_your_api_key_here";

    private final String apiKey;
    private final String fromEmail;
    private final String sendEmailUrl;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Autowired
    public BrevoEmailService(
            @Value("${brevo.api-key}") String apiKey,
            @Value("${brevo.from-email}") String fromEmail,
            ObjectMapper objectMapper) {
        this(apiKey, fromEmail, objectMapper, DEFAULT_BREVO_SEND_EMAIL_URL);
    }

    /**
     * Test-visible constructor allowing the Brevo endpoint to be pointed at a local
     * stand-in server, so unit tests can exercise the real request/response handling
     * without depending on network access to the live Brevo API.
     */
    BrevoEmailService(String apiKey, String fromEmail, ObjectMapper objectMapper, String sendEmailUrl) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        this.objectMapper = objectMapper;
        this.sendEmailUrl = sendEmailUrl;
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

        send("PasswordReset", toEmail, "Restablecer contraseña - Rent2Go", html,
                "Failed to send password reset email");
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

        send("EmailVerification", toEmail, "Verifica tu cuenta - Rent2Go", html,
                "Failed to send verification email");
    }

    private void send(String logTag, String toEmail, String subject, String html, String failureMessage) {
        if (isBlankOrPlaceholder(apiKey)) {
            log.error("[{}] Brevo is not configured: api-key is missing or a placeholder. " +
                            "Set the BREVO_API_KEY environment variable. Email to {} was NOT sent.",
                    logTag, toEmail);
            throw new IllegalStateException("Email sending is not configured on the server (Brevo API key missing).");
        }

        try {
            String requestBody = buildRequestBody(toEmail, subject, html);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sendEmailUrl))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String messageId = extractMessageId(response.body());
                log.info("[{}] Email sent to {} | messageId={}", logTag, toEmail, messageId);
            } else {
                log.error("[{}] Brevo send failed: status={} body={}", logTag, response.statusCode(), response.body());
                throw new RuntimeException(failureMessage);
            }
        } catch (IOException e) {
            log.error("[{}] Failed to send email to {}: {}", logTag, toEmail, e.getMessage());
            throw new RuntimeException(failureMessage, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[{}] Interrupted while sending email to {}: {}", logTag, toEmail, e.getMessage());
            throw new RuntimeException(failureMessage, e);
        }
    }

    private String buildRequestBody(String toEmail, String subject, String html) throws IOException {
        ObjectNode root = objectMapper.createObjectNode();

        ObjectNode sender = root.putObject("sender");
        sender.put("email", fromEmail);

        ObjectNode recipient = objectMapper.createObjectNode();
        recipient.put("email", toEmail);
        root.putArray("to").add(recipient);

        root.put("subject", subject);
        root.put("htmlContent", html);

        return objectMapper.writeValueAsString(root);
    }

    private String extractMessageId(String responseBody) {
        try {
            JsonNode node = objectMapper.readTree(responseBody);
            JsonNode id = node.get("messageId");
            return id != null ? id.asText() : "unknown";
        } catch (IOException e) {
            return "unknown";
        }
    }

    private boolean isBlankOrPlaceholder(String value) {
        return value == null || value.isBlank() || value.equals(PLACEHOLDER_API_KEY);
    }
}
