package app.web.rtgtechnologies.rent2go.iam.interfaces.rest.transform;

import org.springframework.stereotype.Component;

import app.web.rtgtechnologies.rent2go.iam.domain.model.commands.RegisterUserCommand;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources.RegisterUserResource;

@Component
public class RegisterUserCommandFromResourceAssembler {
    public RegisterUserCommand toCommandFromResource(RegisterUserResource resource) {
        return new RegisterUserCommand(
                resource.email(),
                resource.password(),
                resource.username()
        );
    }
}
