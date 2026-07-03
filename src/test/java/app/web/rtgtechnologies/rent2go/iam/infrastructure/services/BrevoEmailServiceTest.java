package app.web.rtgtechnologies.rent2go.iam.infrastructure.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link BrevoEmailService}.
 * <p>
 * ResendEmailService (the class this replaces as @Primary) had no unit test coverage
 * prior to this migration, so this establishes coverage from scratch: the fail-fast
 * guard on placeholder/missing API keys (the exact class of bug that caused the
 * Cloudinary silent-failure incident on profile-photo upload), and a successful send
 * verified against a real loopback HTTP server standing in for Brevo's API. Using the
 * JDK's own HttpServer avoids adding a mocking-library dependency just for this test.
 */
class BrevoEmailServiceTest {

    private static final String VALID_TOKEN = "123456";
    private static final String TO_EMAIL = "user@example.com";
    private static final String FROM_EMAIL = "noreply@rent2go.test";

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void sendVerificationEmail_withPlaceholderApiKey_failsFastAndLogsLoudly() {
        BrevoEmailService service = new BrevoEmailService(
                "brevo_your_api_key_here", FROM_EMAIL, new ObjectMapper(),
                BrevoEmailService.DEFAULT_BREVO_SEND_EMAIL_URL);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.sendVerificationEmail(TO_EMAIL, VALID_TOKEN));

        assertTrue(ex.getMessage().toLowerCase().contains("brevo"),
                "Exception message should clearly indicate Brevo misconfiguration, not fail silently");
    }

    @Test
    void sendPasswordResetEmail_withBlankApiKey_failsFastAndLogsLoudly() {
        BrevoEmailService service = new BrevoEmailService(
                "", FROM_EMAIL, new ObjectMapper(), BrevoEmailService.DEFAULT_BREVO_SEND_EMAIL_URL);

        assertThrows(IllegalStateException.class,
                () -> service.sendPasswordResetEmail(TO_EMAIL, VALID_TOKEN));
    }

    @Test
    void sendPasswordResetEmail_withNullApiKey_failsFastAndLogsLoudly() {
        BrevoEmailService service = new BrevoEmailService(
                null, FROM_EMAIL, new ObjectMapper(), BrevoEmailService.DEFAULT_BREVO_SEND_EMAIL_URL);

        assertThrows(IllegalStateException.class,
                () -> service.sendPasswordResetEmail(TO_EMAIL, VALID_TOKEN));
    }

    @Test
    void sendVerificationEmail_withValidApiKey_postsToConfiguredEndpointAndSucceeds() throws IOException {
        AtomicReference<String> capturedBody = new AtomicReference<>();
        AtomicReference<String> capturedApiKeyHeader = new AtomicReference<>();

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v3/smtp/email", exchange -> {
            byte[] requestBytes = exchange.getRequestBody().readAllBytes();
            capturedBody.set(new String(requestBytes, StandardCharsets.UTF_8));
            capturedApiKeyHeader.set(exchange.getRequestHeaders().getFirst("api-key"));

            byte[] responseBytes = "{\"messageId\":\"test-message-id-123\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(201, responseBytes.length);
            exchange.getResponseBody().write(responseBytes);
            exchange.close();
        });
        server.start();

        String testUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/v3/smtp/email";
        BrevoEmailService service = new BrevoEmailService(
                "real-test-api-key", FROM_EMAIL, new ObjectMapper(), testUrl);

        service.sendVerificationEmail(TO_EMAIL, VALID_TOKEN);

        assertEquals("real-test-api-key", capturedApiKeyHeader.get());
        assertTrue(capturedBody.get().contains(TO_EMAIL));
        assertTrue(capturedBody.get().contains(VALID_TOKEN));
        assertTrue(capturedBody.get().contains("Verifica tu cuenta - Rent2Go"));
    }

    @Test
    void sendPasswordResetEmail_withValidApiKey_postsToConfiguredEndpointAndSucceeds() throws IOException {
        AtomicReference<String> capturedBody = new AtomicReference<>();

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v3/smtp/email", exchange -> {
            byte[] requestBytes = exchange.getRequestBody().readAllBytes();
            capturedBody.set(new String(requestBytes, StandardCharsets.UTF_8));

            byte[] responseBytes = "{\"messageId\":\"test-message-id-456\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(201, responseBytes.length);
            exchange.getResponseBody().write(responseBytes);
            exchange.close();
        });
        server.start();

        String testUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/v3/smtp/email";
        BrevoEmailService service = new BrevoEmailService(
                "real-test-api-key", FROM_EMAIL, new ObjectMapper(), testUrl);

        service.sendPasswordResetEmail(TO_EMAIL, VALID_TOKEN);

        assertTrue(capturedBody.get().contains(TO_EMAIL));
        assertTrue(capturedBody.get().contains(VALID_TOKEN));
        assertTrue(capturedBody.get().contains("Restablecer contraseña - Rent2Go"));
    }

    @Test
    void send_whenBrevoReturnsErrorStatus_throwsAndDoesNotSwallowFailure() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v3/smtp/email", exchange -> {
            byte[] responseBytes = "{\"code\":\"unauthorized\"}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(401, responseBytes.length);
            exchange.getResponseBody().write(responseBytes);
            exchange.close();
        });
        server.start();

        String testUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/v3/smtp/email";
        BrevoEmailService service = new BrevoEmailService(
                "invalid-api-key", FROM_EMAIL, new ObjectMapper(), testUrl);

        assertThrows(RuntimeException.class,
                () -> service.sendVerificationEmail(TO_EMAIL, VALID_TOKEN));
    }
}
