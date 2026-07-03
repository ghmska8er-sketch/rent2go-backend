package app.web.rtgtechnologies.rent2go.iam.domain.model.aggregates;

import app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.AccountType;
import app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.Email;
import app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.Password;
import app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.Username;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * UserTest — phone_verified computation.
 *
 * Clarified scope (2026-07-02): phone_verified is NOT SMS/OTP proof-of-possession.
 * It is a rule-based format/presence check: true only when the user has a phone number
 * registered AND it is a syntactically valid Peru mobile number (9 digits, starts with 9,
 * e.g. "932400537"). False when missing, cleared, or malformed. No SMS infrastructure
 * is introduced or required for this rule.
 *
 * phone_verified is computed automatically inside User.setPhone() (see User.java), so it
 * cannot drift from the actual phone value — there is no separate/manual setter to test.
 */
class UserTest {

    private User newUser(String phone) {
        return new User(
                new Email("renter@example.com"),
                new Password("Str0ngPassw0rd!"),
                new Username("renter_one"),
                "Renter One",
                phone,
                null,
                AccountType.RENTER
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"932400537", "999999999", "900000000"})
    void validPeruMobileNumber_isVerifiedAtRegistration(String validPhone) {
        User user = newUser(validPhone);

        assertTrue(user.isPhoneVerified());
        assertTrue(user.getPhoneVerified());
    }

    @ParameterizedTest
    @ValueSource(strings = {"93240053", "9324005377", "12345678901"})
    void wrongLengthNumber_isNotVerified(String wrongLengthPhone) {
        User user = newUser(wrongLengthPhone);

        assertFalse(user.isPhoneVerified());
    }

    @ParameterizedTest
    @ValueSource(strings = {"93240053a", "9324-0053", "abcdefghi"})
    void nonNumericCharacters_isNotVerified(String nonNumericPhone) {
        User user = newUser(nonNumericPhone);

        assertFalse(user.isPhoneVerified());
    }

    @ParameterizedTest
    @ValueSource(strings = {"832400537", "132400537", "032400537"})
    void numberNotStartingWithNine_isNotVerified(String wrongPrefixPhone) {
        User user = newUser(wrongPrefixPhone);

        assertFalse(user.isPhoneVerified());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void nullOrEmptyPhone_isNotVerified(String emptyPhone) {
        User user = newUser(emptyPhone);

        assertFalse(user.isPhoneVerified());
    }

    @ParameterizedTest
    @CsvSource({
            "932400537, true",
            "812345678, false"
    })
    void settingPhone_recomputesVerifiedFlag(String phone, boolean expectedVerified) {
        User user = newUser(null);
        assertFalse(user.isPhoneVerified());

        user.setPhone(phone);

        assertTrue(user.isPhoneVerified() == expectedVerified);
    }

    @org.junit.jupiter.api.Test
    void clearingPhoneViaProfileUpdate_flipsVerifiedToFalse() {
        User user = newUser("932400537");
        assertTrue(user.isPhoneVerified());

        // Mirrors PATCH /auth/me's explicit-clear semantics: phone param present but blank -> null.
        user.setPhone(null);

        assertFalse(user.isPhoneVerified());
    }
}
