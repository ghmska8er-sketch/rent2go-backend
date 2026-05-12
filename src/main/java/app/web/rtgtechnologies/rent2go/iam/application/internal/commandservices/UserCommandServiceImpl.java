package app.web.rtgtechnologies.rent2go.iam.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.web.rtgtechnologies.rent2go.iam.domain.model.aggregates.User;
import app.web.rtgtechnologies.rent2go.iam.domain.model.commands.LoginCommand;
import app.web.rtgtechnologies.rent2go.iam.domain.model.commands.RegisterUserCommand;
import app.web.rtgtechnologies.rent2go.iam.domain.model.commands.VerifyEmailCommand;
import app.web.rtgtechnologies.rent2go.iam.domain.model.services.UserCommandService;
import app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.Email;
import app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.Password;
import app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.UserStatus;
import app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.Username;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.services.JwtTokenProvider;

@Service
@Transactional
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public UserCommandServiceImpl(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Long handle(RegisterUserCommand command) {
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

        User user = new User(email, password, username);

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
}
