package app.web.rtgtechnologies.rent2go.iam.domain.model.aggregates;

import app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.Email;
import app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.AccountType;
import app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.Password;
import app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.UserStatus;
import app.web.rtgtechnologies.rent2go.iam.domain.model.valueobjects.Username;
import app.web.rtgtechnologies.rent2go.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User extends AuditableAbstractAggregateRoot<User> {

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", nullable = false, unique = true, length = 255))
    private Email email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "username", nullable = false, unique = true, length = 50))
    private Username username;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private UserStatus status;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified;

    @Column(name = "phone_verified", nullable = false)
    private Boolean phoneVerified;

    @Column(name = "two_factor_enabled")
    private Boolean twoFactorEnabled;

    @Column(name = "kyc_verified", nullable = false)
    private Boolean kycVerified;

    public User() {
    }

    public User(Email email, Password password, Username username, String fullName, String phone, String profileImageUrl, AccountType accountType) {
        this.email = email;
        this.passwordHash = password.getHashedValue();
        this.username = username;
        this.fullName = fullName;
        this.phone = phone;
        this.profileImageUrl = profileImageUrl;
        this.accountType = accountType;
        this.status = UserStatus.PENDING_VERIFICATION;
        this.emailVerified = false;
        this.phoneVerified = false;
        this.twoFactorEnabled = false;
        this.kycVerified = false;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Username getUsername() {
        return username;
    }

    public void setUsername(Username username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Boolean getPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(Boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }

    public Boolean getTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(Boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public Boolean getKycVerified() {
        return kycVerified;
    }

    public void setKycVerified(Boolean kycVerified) {
        this.kycVerified = kycVerified;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = UserStatus.SUSPENDED;
    }

    public void block() {
        this.status = UserStatus.BLOCKED;
    }

    public boolean isActive() {
        return UserStatus.ACTIVE.equals(this.status);
    }

    public boolean isBlocked() {
        return UserStatus.BLOCKED.equals(this.status);
    }

    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(this.emailVerified);
    }

    public boolean isPhoneVerified() {
        return Boolean.TRUE.equals(this.phoneVerified);
    }

    public boolean isKycVerified() {
        return Boolean.TRUE.equals(this.kycVerified);
    }
}
