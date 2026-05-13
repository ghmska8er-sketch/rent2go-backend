package app.web.rtgtechnologies.rent2go.iam.domain.model.commands;

public record VerifyEmailCommand(
        Long userId,
        String token
) {
}
