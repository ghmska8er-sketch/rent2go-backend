package app.web.rtgtechnologies.rent2go.iam.domain.model.services;

import java.util.Optional;

import app.web.rtgtechnologies.rent2go.iam.domain.model.aggregates.User;
import app.web.rtgtechnologies.rent2go.iam.domain.model.queries.GetUserByEmailQuery;
import app.web.rtgtechnologies.rent2go.iam.domain.model.queries.GetUserByIdQuery;
import app.web.rtgtechnologies.rent2go.iam.domain.model.queries.ValidateTokenQuery;

public interface UserQueryService {
    Optional<User> handle(GetUserByEmailQuery query);
    Optional<User> handle(GetUserByIdQuery query);
    Long handle(ValidateTokenQuery query);
}
