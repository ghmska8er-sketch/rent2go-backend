package app.web.rtgtechnologies.rent2go.iam.domain.model.services;

import app.web.rtgtechnologies.rent2go.iam.domain.model.commands.LoginCommand;
import app.web.rtgtechnologies.rent2go.iam.domain.model.commands.RegisterUserCommand;
import app.web.rtgtechnologies.rent2go.iam.domain.model.commands.VerifyEmailCommand;

public interface UserCommandService {
    Long handle(RegisterUserCommand command);
    String handle(LoginCommand command);
    void handle(VerifyEmailCommand command);
}
