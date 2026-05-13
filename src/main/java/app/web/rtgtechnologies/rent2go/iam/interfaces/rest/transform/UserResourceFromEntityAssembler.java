package app.web.rtgtechnologies.rent2go.iam.interfaces.rest.transform;

import org.springframework.stereotype.Component;

import app.web.rtgtechnologies.rent2go.iam.domain.model.aggregates.User;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources.UserResource;

@Component
public class UserResourceFromEntityAssembler {
    public UserResource toResourceFromEntity(User user) {
        return new UserResource(
                user.getId(),
                user.getEmail().getValue(),
                user.getUsername().getValue(),
                user.getStatus().toString(),
                user.getEmailVerified(),
                user.getTwoFactorEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
