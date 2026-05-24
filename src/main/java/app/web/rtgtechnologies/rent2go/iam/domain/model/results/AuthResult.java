package app.web.rtgtechnologies.rent2go.iam.domain.model.results;

public record AuthResult(String jwt, Long userId, String email, String username) {}
