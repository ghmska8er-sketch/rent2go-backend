package app.web.rtgtechnologies.rent2go.iam.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.web.rtgtechnologies.rent2go.iam.domain.model.aggregates.User;
import app.web.rtgtechnologies.rent2go.iam.domain.model.queries.GetUserByEmailQuery;
import app.web.rtgtechnologies.rent2go.iam.domain.model.queries.GetUserByIdQuery;
import app.web.rtgtechnologies.rent2go.iam.domain.model.queries.ValidateTokenQuery;
import app.web.rtgtechnologies.rent2go.iam.domain.model.services.UserQueryService;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.services.JwtTokenProvider;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public UserQueryServiceImpl(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Optional<User> handle(GetUserByEmailQuery query) {
        return userRepository.findByEmail_Value(query.email());
    }

    @Override
    public Optional<User> handle(GetUserByIdQuery query) {
        return userRepository.findById(query.userId());
    }

    @Override
    public Long handle(ValidateTokenQuery query) {
        if (!jwtTokenProvider.validateToken(query.token())) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        return jwtTokenProvider.extractUserIdFromToken(query.token());
    }
}
