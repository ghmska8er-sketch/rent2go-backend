package app.web.rtgtechnologies.rent2go.iam.domain.model.commands;

public record ResetPasswordCommand(
        String token,
        String newPassword
) {
}
