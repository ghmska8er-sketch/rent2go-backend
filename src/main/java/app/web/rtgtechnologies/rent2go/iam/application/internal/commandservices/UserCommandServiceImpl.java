package app.web.rtgtechnologies.rent2go.iam.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.web.rtgtechnologies.rent2go.iam.domain.model.aggregates.User;
import app.web.rtgtechnologies.rent2go.iam.domain.model.commands.LoginCommand;
import app.web.rtgtechnologies.rent2go.iam.domain.model.commands.RegisterUserCommand;
import app.web.rtgtechnologies.rent2go.iam.domain.model.commands.VerifyEmailCommand;
import app.web.rtgtechnologies.rent2go.iam.domain.model.services.UserCommandService;
import app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.AccountType;
import app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.Email;
import app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.Password;
import app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.UserStatus;
import app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.Username;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.PasswordResetTokenRepository;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.entities.PasswordResetToken;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.services.JwtTokenProvider;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.services.EmailService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Transactional
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.KycApplicationRepository kycApplicationRepository;
    private final app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.TwoFactorTokenRepository twoFactorTokenRepository;
    private final EmailService emailService;

    public UserCommandServiceImpl(UserRepository userRepository, JwtTokenProvider jwtTokenProvider,
                                  PasswordResetTokenRepository passwordResetTokenRepository,
                                  app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.KycApplicationRepository kycApplicationRepository,
                                  app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.TwoFactorTokenRepository twoFactorTokenRepository,
                                  EmailService emailService) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.kycApplicationRepository = kycApplicationRepository;
        this.twoFactorTokenRepository = twoFactorTokenRepository;
        this.emailService = emailService;
    }

    @Override
    public Long handle(RegisterUserCommand command) {
        if (command.accountType() == null) {
            throw new IllegalArgumentException("Account type is required");
        }
        if (command.accountType() != AccountType.OWNER
                && command.accountType() != AccountType.RENTER
                && command.accountType() != AccountType.BOTH
                && command.accountType() != AccountType.ADMIN) {
            throw new IllegalArgumentException("Account type must be OWNER, RENTER, BOTH or ADMIN");
        }

        // Validar que el correo no exista
        if (userRepository.existsByEmail_Value(command.email())) {
            throw new IllegalArgumentException("Email already registered: " + command.email());
        }

        // Validar que el username no exista
        if (userRepository.existsByUsername_Value(command.username())) {
            throw new IllegalArgumentException("Username already taken: " + command.username());
        }

        // Crear agregado User con value objects
        Email email = new Email(command.email());
        Password password = new Password(command.password());
        Username username = new Username(command.username());

        User user = new User(
            email,
            password,
            username,
            command.fullName(),
            command.phone(),
            command.profileImageUrl(),
            command.accountType()
        );

        // Guardar
        User savedUser = userRepository.save(user);

        return savedUser.getId();
    }

    @Override
    public String handle(LoginCommand command) {
        // Buscar usuario por email
        User user = userRepository.findByEmail_Value(command.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + command.email()));

        // Verificar estado
        if (user.isBlocked()) {
            throw new IllegalArgumentException("User is blocked: " + command.email());
        }

        if (!user.isActive() && !UserStatus.PENDING_VERIFICATION.equals(user.getStatus())) {
            throw new IllegalArgumentException("User is inactive: " + command.email());
        }

        // Validar contraseña
        Password password = Password.fromHash(user.getPasswordHash());
        if (!password.matches(command.password())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        // If user has two-factor enabled, issue a short-lived login token and send via email
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            // remove existing login tokens
            twoFactorTokenRepository.deleteByUserIdAndPurpose(user.getId(), "LOGIN");
            String token = UUID.randomUUID().toString();
            Instant now = Instant.now();
            Instant expiresAt = now.plus(5, ChronoUnit.MINUTES);
            var tf = new app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.entities.TwoFactorToken(token, user.getId(), "LOGIN", expiresAt, now);
            twoFactorTokenRepository.save(tf);
            emailService.sendPasswordResetEmail(user.getEmail().getValue(), token); // reuse email service for delivery
            return "2FA_REQUIRED";
        }

        // Generar token JWT
        return jwtTokenProvider.generateToken(user.getId(), user.getEmail().getValue());
    }

    @Override
    public void handle(VerifyEmailCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + command.userId()));

        // Marcar email como verificado
        user.setEmailVerified(true);

        // Activar usuario
        user.activate();

        userRepository.save(user);
    }

    @Override
    public Long handle(app.web.rtgtechnologies.rent2go.iam.domain.model.commands.SubmitKycCommand command) {
        var user = userRepository.findById(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + command.userId()));

        var now = Instant.now();
        var entity = new app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.entities.KycApplication(
                command.userId(), command.fullName(), command.idNumber(), command.documentUrl(), "PENDING", now
        );

        var saved = kycApplicationRepository.save(entity);

        // mark user as pending verification if not already
        if (!app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.UserStatus.PENDING_VERIFICATION.equals(user.getStatus())) {
            user.setStatus(app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.UserStatus.PENDING_VERIFICATION);
            userRepository.save(user);
        }

        return saved.getId();
    }

    @Override
    public String handle(app.web.rtgtechnologies.rent2go.iam.domain.model.commands.RequestPasswordResetCommand command) {
        var userOpt = userRepository.findByEmail_Value(command.email());
        if (userOpt.isEmpty()) {
            // Do not reveal whether email exists
            return "ok";
        }

        var user = userOpt.get();

        // Remove existing tokens for user
        passwordResetTokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(1, ChronoUnit.HOURS);

        PasswordResetToken entity = new PasswordResetToken(token, user.getId(), expiresAt, now);
        passwordResetTokenRepository.save(entity);

        // Send email (logged)
        emailService.sendPasswordResetEmail(user.getEmail().getValue(), token);

        return "ok";
    }

    @Override
    public void handle(app.web.rtgtechnologies.rent2go.iam.domain.model.commands.ResetPasswordCommand command) {
        var tokenOpt = passwordResetTokenRepository.findByToken(command.token());
        var tokenEntity = tokenOpt.orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (tokenEntity.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Token expired");
        }

        var user = userRepository.findById(tokenEntity.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Validate new password via Password value object
        app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.Password newPassword =
                new app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.Password(command.newPassword());

        user.setPasswordHash(newPassword.getHashedValue());
        userRepository.save(user);

        // remove token
        passwordResetTokenRepository.deleteByUserId(user.getId());
    }

    @Override
    public String initiateTwoFactorLogin(String email) {
        var userOpt = userRepository.findByEmail_Value(email);
        if (userOpt.isEmpty()) return "ok";
        var user = userOpt.get();
        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled())) return "not_enabled";

        twoFactorTokenRepository.deleteByUserIdAndPurpose(user.getId(), "LOGIN");
        String token = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(5, ChronoUnit.MINUTES);
        var tf = new app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.entities.TwoFactorToken(token, user.getId(), "LOGIN", expiresAt, now);
        twoFactorTokenRepository.save(tf);
        emailService.sendPasswordResetEmail(user.getEmail().getValue(), token);
        return "ok";
    }

    @Override
    public void initiateEnableTwoFactor(Long userId) {
        var user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        twoFactorTokenRepository.deleteByUserIdAndPurpose(userId, "ENABLE");
        String token = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(10, ChronoUnit.MINUTES);
        var tf = new app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.entities.TwoFactorToken(token, userId, "ENABLE", expiresAt, now);
        twoFactorTokenRepository.save(tf);
        emailService.sendPasswordResetEmail(user.getEmail().getValue(), token);
    }

    @Override
    public void confirmEnableTwoFactor(String token) {
        var tokenOpt = twoFactorTokenRepository.findByToken(token);
        var te = tokenOpt.orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        if (te.getExpiresAt().isBefore(Instant.now())) throw new IllegalArgumentException("Token expired");
        var user = userRepository.findById(te.getUserId()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setTwoFactorEnabled(true);
        userRepository.save(user);
        twoFactorTokenRepository.deleteByUserIdAndPurpose(user.getId(), "ENABLE");
    }

    @Override
    public app.web.rtgtechnologies.rent2go.iam.domain.model.results.AuthResult verifyTwoFactorLogin(String token) {
        var tokenOpt = twoFactorTokenRepository.findByToken(token);
        var te = tokenOpt.orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        if (!"LOGIN".equals(te.getPurpose())) throw new IllegalArgumentException("Invalid purpose");
        if (te.getExpiresAt().isBefore(Instant.now())) throw new IllegalArgumentException("Token expired");

        var user = userRepository.findById(te.getUserId()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        // consume token
        twoFactorTokenRepository.deleteByUserIdAndPurpose(user.getId(), te.getPurpose());

        // generate JWT and return user info
        String jwt = jwtTokenProvider.generateToken(user.getId(), user.getEmail().getValue());
        return new app.web.rtgtechnologies.rent2go.iam.domain.model.results.AuthResult(jwt, user.getId(), user.getEmail().getValue(), user.getUsername().getValue());
    }
}
