package app.web.rtgtechnologies.rent2go.iam.domain.model.commands;

public record LoginCommand(
        String email,
        String password
) {
}
