package app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources;

public record RegisterUserResource(
        String email,
        String password,
        String username
) {
}
