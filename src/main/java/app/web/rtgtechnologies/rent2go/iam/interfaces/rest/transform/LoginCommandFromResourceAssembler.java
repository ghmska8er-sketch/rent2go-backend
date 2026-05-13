package app.web.rtgtechnologies.rent2go.iam.interfaces.rest.transform;

import org.springframework.stereotype.Component;

import app.web.rtgtechnologies.rent2go.iam.domain.model.commands.LoginCommand;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources.LoginResource;

@Component
public class LoginCommandFromResourceAssembler {
    public LoginCommand toCommandFromResource(LoginResource resource) {
        return new LoginCommand(
                resource.email(),
                resource.password()
        );
    }
}
