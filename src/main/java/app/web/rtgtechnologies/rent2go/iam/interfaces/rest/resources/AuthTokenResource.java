package app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources;

public record AuthTokenResource(
        String token,
        Long userId,
        String email,
        String username
) {
}
