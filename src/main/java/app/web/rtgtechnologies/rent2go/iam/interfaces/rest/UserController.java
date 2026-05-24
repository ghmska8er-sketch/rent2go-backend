package app.web.rtgtechnologies.rent2go.iam.interfaces.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
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
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources.VerifyEmailResource;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources.PasswordResetRequestResource;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources.PasswordResetConfirmResource;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources.SubmitKycResource;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.transform.AuthTokenResourceFromUserAssembler;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.transform.LoginCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.transform.RegisterUserCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import jakarta.validation.Valid;

@Tag(name = "IAM", description = "Identity and access management operations")
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

    @PostMapping("/verify")
    @Operation(summary = "Verify email", description = "Verifies a user's email address using the confirmation token.")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailResource resource) {
        try {
            userCommandService.handle(new app.web.rtgtechnologies.rent2go.iam.domain.model.commands.VerifyEmailCommand(
                    resource.userId(), resource.token()
            ));
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/password/request")
    @Operation(summary = "Request password reset", description = "Starts the password recovery flow by sending a reset token to the user's email.")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequestResource resource) {
        userCommandService.handle(new app.web.rtgtechnologies.rent2go.iam.domain.model.commands.RequestPasswordResetCommand(resource.email()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password/reset")
    @Operation(summary = "Confirm password reset", description = "Completes the password reset flow using the received token and a new password.")
    public ResponseEntity<Void> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmResource resource) {
        try {
            userCommandService.handle(new app.web.rtgtechnologies.rent2go.iam.domain.model.commands.ResetPasswordCommand(resource.token(), resource.newPassword()));
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Creates a new Rent2Go user account and returns the created profile.")
    public ResponseEntity<UserResource> registerUser(@Valid @RequestBody RegisterUserResource resource) {
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

    @PostMapping("/kyc")
    @Operation(summary = "Submit KYC", description = "Submits identity verification data for review.")
    public ResponseEntity<Void> submitKyc(@Valid @RequestBody SubmitKycResource resource) {
        Long id = userCommandService.handle(new app.web.rtgtechnologies.rent2go.iam.domain.model.commands.SubmitKycCommand(
            resource.userId(), resource.fullName(), resource.idNumber(), resource.dniFrontUrl(), resource.dniBackUrl(), resource.driverLicenseUrl()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates the user and returns the JWT token payload.")
    public ResponseEntity<AuthTokenResource> login(@RequestBody LoginResource resource) {
        try {
            LoginCommand command = loginAssembler.toCommandFromResource(resource);
            String tokenOrFlag = userCommandService.handle(command);

            User user = userQueryService.handle(new app.web.rtgtechnologies.rent2go.iam.domain.model.queries.GetUserByEmailQuery(resource.email()))
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            AuthTokenResource response = authTokenAssembler.toResourceFromUser(user, tokenOrFlag);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns the authenticated user's profile using the Bearer token.")
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
