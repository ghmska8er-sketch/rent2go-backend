package app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Password {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private static final int MIN_LENGTH = 8;

    private final String hashedValue;

    public Password(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (plainPassword.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_LENGTH + " characters long");
        }
        this.hashedValue = encoder.encode(plainPassword);
    }

    private Password(String hashedValue, boolean isHashed) {
        if (isHashed) {
            this.hashedValue = hashedValue;
        } else {
            throw new IllegalArgumentException("Invalid constructor call");
        }
    }

    public static Password fromHash(String hashedValue) {
        return new Password(hashedValue, true);
    }

    public String getHashedValue() {
        return hashedValue;
    }

    public boolean matches(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            return false;
        }
        return encoder.matches(plainPassword, this.hashedValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Password password = (Password) o;
        return hashedValue.equals(password.hashedValue);
    }

    @Override
    public int hashCode() {
        return hashedValue.hashCode();
    }
}
