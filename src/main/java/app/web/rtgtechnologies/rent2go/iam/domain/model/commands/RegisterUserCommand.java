package app.web.rtgtechnologies.rent2go.iam.domain.model.commands;

public record RegisterUserCommand(
        String email,
        String password,
        String username
) {
}
