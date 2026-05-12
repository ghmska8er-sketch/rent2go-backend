package app.web.rtgtechnologies.rent2go.iam.interfaces.rest.transform;

import org.springframework.stereotype.Component;

import app.web.rtgtechnologies.rent2go.iam.domain.model.aggregates.User;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources.AuthTokenResource;

@Component
public class AuthTokenResourceFromUserAssembler {
    public AuthTokenResource toResourceFromUser(User user, String token) {
        return new AuthTokenResource(
                token,
                user.getId(),
                user.getEmail().getValue(),
                user.getUsername().getValue()
        );
    }
}
