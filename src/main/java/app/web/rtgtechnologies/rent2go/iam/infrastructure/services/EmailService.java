package app.web.rtgtechnologies.rent2go.iam.infrastructure.services;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String token);
}
