package app.web.rtgtechnologies.rent2go.iam.domain.model.services;

import app.web.rtgtechnologies.rent2go.iam.domain.model.commands.LoginCommand;
import app.web.rtgtechnologies.rent2go.iam.domain.model.commands.RegisterUserCommand;
import app.web.rtgtechnologies.rent2go.iam.domain.model.commands.VerifyEmailCommand;
import app.web.rtgtechnologies.rent2go.iam.domain.model.commands.RequestPasswordResetCommand;
import app.web.rtgtechnologies.rent2go.iam.domain.model.commands.ResetPasswordCommand;
import app.web.rtgtechnologies.rent2go.iam.domain.model.commands.ResendVerificationCommand;

public interface UserCommandService {
    Long handle(RegisterUserCommand command);
    String handle(LoginCommand command);
    void handle(VerifyEmailCommand command);
    void handle(ResendVerificationCommand command);
    Long handle(app.web.rtgtechnologies.rent2go.iam.domain.model.commands.SubmitKycCommand command);
    String initiateTwoFactorLogin(String email);
    void confirmEnableTwoFactor(String token);
    void initiateEnableTwoFactor(Long userId);
    app.web.rtgtechnologies.rent2go.iam.domain.model.results.AuthResult verifyTwoFactorLogin(String token);
    String handle(RequestPasswordResetCommand command);
    void handle(ResetPasswordCommand command);
}
