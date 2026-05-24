package app.web.rtgtechnologies.rent2go.iam.interfaces.rest.transform;

import org.springframework.stereotype.Component;

import app.web.rtgtechnologies.rent2go.iam.domain.model.commands.RegisterUserCommand;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources.RegisterUserResource;

@Component
public class RegisterUserCommandFromResourceAssembler {
    public RegisterUserCommand toCommandFromResource(RegisterUserResource resource) {
        String username = resource.username();
        if (username == null || username.isBlank()) {
            String base = resource.fullName() == null ? "user" : resource.fullName().toLowerCase().replaceAll("[^a-z0-9]", ".");
            username = base + "_" + (System.currentTimeMillis() % 10000);
        }

        return new RegisterUserCommand(
                resource.email(),
                resource.password(),
                username,
            resource.fullName(),
            resource.phone(),
            resource.profileImageUrl(),
                resource.accountType()
        );
    }
}
