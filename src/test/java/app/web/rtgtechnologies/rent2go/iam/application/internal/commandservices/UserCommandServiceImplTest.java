package app.web.rtgtechnologies.rent2go.iam.application.internal.commandservices;

import app.web.rtgtechnologies.rent2go.iam.domain.model.aggregates.User;
import app.web.rtgtechnologies.rent2go.iam.domain.model.commands.RegisterUserCommand;
import app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.AccountType;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.entities.EmailVerificationToken;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.EmailVerificationTokenRepository;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.KycApplicationRepository;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.PasswordResetTokenRepository;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.TwoFactorTokenRepository;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.services.EmailService;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.services.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UserCommandServiceImplTest
 *
 * Regression coverage for the register() flow, added after a production incident:
 * a misconfigured/unavailable email provider (Resend) caused sendVerificationEmail
 * to throw a RuntimeException, which propagated out of handle(RegisterUserCommand)
 * uncaught and surfaced to clients as a generic HTTP 500 on POST /api/v1/auth/register,
 * for every AccountType.
 *
 * All IO (repositories, email, JWT) is mocked; no Spring context is loaded.
 */
@ExtendWith(MockitoExtension.class)
class UserCommandServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private KycApplicationRepository kycApplicationRepository;

    @Mock
    private TwoFactorTokenRepository twoFactorTokenRepository;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private EmailService emailService;

    private UserCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserCommandServiceImpl(
                userRepository,
                jwtTokenProvider,
                passwordResetTokenRepository,
                kycApplicationRepository,
                twoFactorTokenRepository,
                emailVerificationTokenRepository,
                emailService
        );
    }

    private RegisterUserCommand validCommand(AccountType accountType) {
        return new RegisterUserCommand(
                "aznska8er@gmail.com",
                "GHMska8er@",
                "aznska8er",
                "Gilbert",
                "936326066",
                "",
                accountType
        );
    }

    @ParameterizedTest
    @EnumSource(value = AccountType.class, names = {"RENTER", "OWNER"})
    void register_succeeds_forRenterAndOwner_whenEmailProviderIsHealthy(AccountType accountType) {
        when(userRepository.existsByEmail_Value(anyString())).thenReturn(false);
        when(userRepository.existsByUsername_Value(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emailVerificationTokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> service.handle(validCommand(accountType)));

        verify(emailService, times(1)).sendVerificationEmail(anyString(), anyString());
    }

    @ParameterizedTest
    @EnumSource(value = AccountType.class, names = {"RENTER", "OWNER"})
    void register_stillSucceeds_forRenterAndOwner_whenEmailProviderFails(AccountType accountType) {
        // Regression test: reproduces the exact production incident. Previously, an unchecked
        // RuntimeException from the email provider (e.g. ResendEmailService wrapping a
        // ResendException when RESEND_API_KEY is missing/invalid) propagated out of handle(),
        // bubbled through UserController and GlobalExceptionHandler.handleAll, and turned a
        // valid registration into an HTTP 500 for the client — even though the user row itself
        // was otherwise valid. Registration must now succeed regardless of email delivery outcome.
        when(userRepository.existsByEmail_Value(anyString())).thenReturn(false);
        when(userRepository.existsByUsername_Value(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emailVerificationTokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("Failed to send verification email"))
                .when(emailService).sendVerificationEmail(anyString(), anyString());

        assertDoesNotThrow(() -> service.handle(validCommand(accountType)));

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_throwsIllegalArgumentException_whenEmailAlreadyRegistered() {
        when(userRepository.existsByEmail_Value(anyString())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.handle(validCommand(AccountType.RENTER)));

        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void register_throwsIllegalArgumentException_whenUsernameAlreadyTaken() {
        when(userRepository.existsByEmail_Value(anyString())).thenReturn(false);
        when(userRepository.existsByUsername_Value(anyString())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.handle(validCommand(AccountType.OWNER)));

        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void register_throwsIllegalArgumentException_whenAccountTypeIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.handle(validCommand(null)));

        verify(userRepository, never()).save(any(User.class));
    }
}
