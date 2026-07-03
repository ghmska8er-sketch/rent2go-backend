package app.web.rtgtechnologies.rent2go.iam.interfaces.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources.PasswordResetResponseResource;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.resources.SubmitKycResource;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.transform.AuthTokenResourceFromUserAssembler;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.transform.LoginCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.transform.RegisterUserCommandFromResourceAssembler;
import app.web.rtgtechnologies.rent2go.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import app.web.rtgtechnologies.rent2go.shared.infrastructure.cloudinary.CloudinaryStorageService;
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
    private final UserRepository userRepository;
    private final CloudinaryStorageService cloudinaryStorageService;

    public UserController(
            UserCommandServiceImpl userCommandService,
            UserQueryServiceImpl userQueryService,
            RegisterUserCommandFromResourceAssembler registerUserAssembler,
            LoginCommandFromResourceAssembler loginAssembler,
            AuthTokenResourceFromUserAssembler authTokenAssembler,
            UserResourceFromEntityAssembler userResourceAssembler,
            UserRepository userRepository,
            CloudinaryStorageService cloudinaryStorageService
    ) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
        this.registerUserAssembler = registerUserAssembler;
        this.loginAssembler = loginAssembler;
        this.authTokenAssembler = authTokenAssembler;
        this.userResourceAssembler = userResourceAssembler;
        this.userRepository = userRepository;
        this.cloudinaryStorageService = cloudinaryStorageService;
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

    @PostMapping("/verify/resend")
    @Operation(summary = "Resend verification email", description = "Issues a new email verification token for the authenticated user and re-sends the verification email (best-effort).")
    public ResponseEntity<Void> resendVerification(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = authHeader.substring(7);
            Long userId = userQueryService.handle(new app.web.rtgtechnologies.rent2go.iam.domain.model.queries.ValidateTokenQuery(token));

            userCommandService.handle(new app.web.rtgtechnologies.rent2go.iam.domain.model.commands.ResendVerificationCommand(userId));
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/password/request")
    @Operation(summary = "Request password reset", description = "Starts the password recovery flow by sending a reset token to the user's email. The token is also returned in the response for testing purposes.")
    public ResponseEntity<PasswordResetResponseResource> requestPasswordReset(@Valid @RequestBody PasswordResetRequestResource resource) {
        String token = userCommandService.handle(new app.web.rtgtechnologies.rent2go.iam.domain.model.commands.RequestPasswordResetCommand(resource.email()));
        return ResponseEntity.ok(new PasswordResetResponseResource(token, "Password reset email sent"));
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
    @Operation(summary = "Submit KYC", description = "Submits identity verification data for review and returns the updated user profile.")
    public ResponseEntity<UserResource> submitKyc(@Valid @RequestBody SubmitKycResource resource) {
        try {
            userCommandService.handle(new app.web.rtgtechnologies.rent2go.iam.domain.model.commands.SubmitKycCommand(
                resource.userId(), resource.fullName(), resource.idNumber(), resource.dniFrontUrl(), resource.dniBackUrl(), resource.driverLicenseUrl()
            ));
            User user = userQueryService.handle(new GetUserByIdQuery(resource.userId()))
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            return ResponseEntity.status(HttpStatus.CREATED).body(userResourceAssembler.toResourceFromEntity(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
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

    // IAM-01: multipart registration — accepts profileImage as a file upload
    @PostMapping(value = "/register/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Register user (multipart)",
               description = "Creates a new user account. Accepts profileImage as a file; all other fields are form fields.")
    public ResponseEntity<UserResource> registerUserMultipart(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String accountType,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) MultipartFile profileImage) {
        try {
            String imageUrl = null;
            if (profileImage != null && !profileImage.isEmpty()) {
                imageUrl = cloudinaryStorageService.upload(profileImage);
            }

            app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.AccountType type =
                app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.AccountType.valueOf(accountType.toUpperCase());

            String resolvedUsername = (username != null && !username.isBlank()) ? username
                : fullName.toLowerCase().replaceAll("[^a-z0-9]", ".") + "_" + (System.currentTimeMillis() % 10000);

            RegisterUserCommand command = new RegisterUserCommand(email, password, resolvedUsername, fullName, phone, imageUrl, type);
            Long userId = userCommandService.handle(command);
            User user = userQueryService.handle(new GetUserByIdQuery(userId))
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            return ResponseEntity.status(HttpStatus.CREATED).body(userResourceAssembler.toResourceFromEntity(user));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // IAM-02: update current user profile with optional image upload
    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update profile (multipart)",
               description = "Updates the authenticated user's profile. All fields are optional; only provided fields are changed.")
    public ResponseEntity<?> updateProfile(
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) MultipartFile profileImage) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String token = authHeader.substring(7);
            Long userId = userQueryService.handle(new app.web.rtgtechnologies.rent2go.iam.domain.model.queries.ValidateTokenQuery(token));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (fullName != null && !fullName.isBlank()) user.setFullName(fullName);
            // phone absent (null) -> untouched; phone present but blank -> explicit clear
            // (phone_verified is recomputed automatically inside setPhone(), including on clear).
            if (phone != null) {
                user.setPhone(phone.isBlank() ? null : phone);
            }
            if (profileImage != null && !profileImage.isEmpty()) {
                String imageUrl = cloudinaryStorageService.upload(profileImage);
                user.setProfileImageUrl(imageUrl);
            }

            userRepository.save(user);
            return ResponseEntity.ok(userResourceAssembler.toResourceFromEntity(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (java.io.IOException e) {
            // Image upload (Cloudinary) failed. Previously this fell into the generic
            // catch below and returned a bodyless 500, which the Flutter client's
            // _extractMessage() could not parse into anything useful — the user just
            // saw a generic "no se pudo actualizar el perfil" with no real signal.
            // Logging here + returning a JSON error body lets the client surface the
            // actual reason (e.g. server misconfiguration) instead of failing silently.
            org.slf4j.LoggerFactory.getLogger(UserController.class)
                    .error("Profile image upload failed for PATCH /auth/me", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of(
                            "error", "Image upload failed",
                            "message", "No se pudo subir la foto de perfil. Intenta nuevamente en unos minutos."));
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(UserController.class)
                    .error("Unexpected error updating profile via PATCH /auth/me", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of(
                            "error", "Internal server error",
                            "message", "No se pudo actualizar el perfil."));
        }
    }

    // IAM-03: KYC submission with 3 document images uploaded directly to Cloudinary
    @PostMapping(value = "/kyc/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Submit KYC (multipart)",
               description = "Submits identity verification. Accepts dniFront, dniBack, and driverLicense as file uploads. Returns the updated user profile.")
    public ResponseEntity<UserResource> submitKycMultipart(
            @RequestParam Long userId,
            @RequestParam String fullName,
            @RequestParam String idNumber,
            @RequestParam MultipartFile dniFront,
            @RequestParam MultipartFile dniBack,
            @RequestParam(required = false) MultipartFile driverLicense) {
        try {
            String dniFrontUrl = cloudinaryStorageService.upload(dniFront);
            String dniBackUrl = cloudinaryStorageService.upload(dniBack);
            String driverLicenseUrl = (driverLicense != null && !driverLicense.isEmpty())
                ? cloudinaryStorageService.upload(driverLicense) : null;

            userCommandService.handle(new app.web.rtgtechnologies.rent2go.iam.domain.model.commands.SubmitKycCommand(
                userId, fullName, idNumber, dniFrontUrl, dniBackUrl, driverLicenseUrl
            ));
            User user = userQueryService.handle(new GetUserByIdQuery(userId))
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            return ResponseEntity.status(HttpStatus.CREATED).body(userResourceAssembler.toResourceFromEntity(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
