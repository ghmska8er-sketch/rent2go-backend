package app.web.rtgtechnologies.rent2go.iam.interfaces.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import app.web.rtgtechnologies.rent2go.iam.application.internal.commandservices.UserCommandServiceImpl;
import app.web.rtgtechnologies.rent2go.iam.application.internal.queryservices.UserQueryServiceImpl;
import app.web.rtgtechnologies.rent2go.iam.domain.model.aggregates.User;
import app.web.rtgtechnologies.rent2go.iam.domain.model.commands.LoginCommand;
import app.web.rtgtechnologies.rent2go.iam.domain.model.commands.RegisterUserCommand;
import app.web.rtgtechnologies.rent2go.iam.domain.model.queries.GetUserByIdQuery;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources.AuthTokenResource;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources.LoginResource;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources.RegisterUserResource;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources.UserResource;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.transform.AuthTokenResourceFromUserAssembler;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.transform.LoginCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.transform.RegisterUserCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;

@Tag(name = "Auth", description = "Authentication operations")
@RestController
@RequestMapping(value = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private final UserCommandServiceImpl userCommandService;
    private final UserQueryServiceImpl userQueryService;
    private final RegisterUserCommandFromResourceAssembler registerUserAssembler;
    private final LoginCommandFromResourceAssembler loginAssembler;
    private final AuthTokenResourceFromUserAssembler authTokenAssembler;
    private final UserResourceFromEntityAssembler userResourceAssembler;

    public UserController(
            UserCommandServiceImpl userCommandService,
            UserQueryServiceImpl userQueryService,
            RegisterUserCommandFromResourceAssembler registerUserAssembler,
            LoginCommandFromResourceAssembler loginAssembler,
            AuthTokenResourceFromUserAssembler authTokenAssembler,
            UserResourceFromEntityAssembler userResourceAssembler
    ) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
        this.registerUserAssembler = registerUserAssembler;
        this.loginAssembler = loginAssembler;
        this.authTokenAssembler = authTokenAssembler;
        this.userResourceAssembler = userResourceAssembler;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResource> registerUser(@RequestBody RegisterUserResource resource) {
        try {
            RegisterUserCommand command = registerUserAssembler.toCommandFromResource(resource);
            Long userId = userCommandService.handle(command);
            User user = userQueryService.handle(new GetUserByIdQuery(userId))
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            UserResource response = userResourceAssembler.toResourceFromEntity(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResource> login(@RequestBody LoginResource resource) {
        try {
            LoginCommand command = loginAssembler.toCommandFromResource(resource);
            String token = userCommandService.handle(command);

            User user = userQueryService.handle(new app.web.rtgtechnologies.rent2go.iam.domain.model.queries.GetUserByEmailQuery(resource.email()))
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            AuthTokenResource response = authTokenAssembler.toResourceFromUser(user, token);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserResource> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = authHeader.substring(7);
            Long userId = userQueryService.handle(new app.web.rtgtechnologies.rent2go.iam.domain.model.queries.ValidateTokenQuery(token));

            User user = userQueryService.handle(new GetUserByIdQuery(userId))
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            UserResource response = userResourceAssembler.toResourceFromEntity(user);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
