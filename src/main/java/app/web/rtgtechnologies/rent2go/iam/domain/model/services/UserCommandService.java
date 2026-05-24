package app.web.rtgtechnologies.rent2go.iam.domain.model.services;

import app.web.rtgtechnologies.rent2go.iam.domain.model.commands.LoginCommand;
import app.web.rtgtechnologies.rent2go.iam.domain.model.commands.RegisterUserCommand;
import app.web.rtgtechnologies.rent2go.iam.domain.model.commands.VerifyEmailCommand;
import app.web.rtgtechnologies.rent2go.iam.domain.model.commands.RequestPasswordResetCommand;
import app.web.rtgtechnologies.rent2go.iam.domain.model.commands.ResetPasswordCommand;

public interface UserCommandService {
    Long handle(RegisterUserCommand command);
    String handle(LoginCommand command);
    void handle(VerifyEmailCommand command);
    String handle(RequestPasswordResetCommand command);
    void handle(ResetPasswordCommand command);
}
