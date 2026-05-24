package app.web.rtgtechnologies.rent2go.iam.domain.model.commands;

import app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.AccountType;

public record RegisterUserCommand(
        String email,
        String password,
        String username,
        AccountType accountType
) {
}
