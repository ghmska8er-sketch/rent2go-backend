package app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "kyc_applications",
    // Perf fix (2026-07-06): user_id backs findByUserIdIn(...)/findFirstByUserIdOrderByCreatedAtDesc,
    // called for every counterparty resolved by CounterpartyResourceAssembler (reservations and
    // vehicle owner-summary listings) and had no index. Same caveat as Reservation/Vehicle's
    // indexes above: no Flyway/Liquibase in this project, so this annotation alone will not
    // apply the index in production (ddl-auto=validate there) — see delivery notes.
    indexes = {
        @Index(name = "idx_kyc_applications_user_id", columnList = "user_id")
    }
)
public class KycApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "id_number", nullable = false)
    private String idNumber;

    @Column(name = "dni_front_url")
    private String dniFrontUrl;

    @Column(name = "dni_back_url")
    private String dniBackUrl;

    @Column(name = "driver_license_url")
    private String driverLicenseUrl;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected KycApplication() {}

    public KycApplication(Long userId, String fullName, String idNumber, String dniFrontUrl, String dniBackUrl, String driverLicenseUrl, String status, Instant createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.idNumber = idNumber;
        this.dniFrontUrl = dniFrontUrl;
        this.dniBackUrl = dniBackUrl;
        this.driverLicenseUrl = driverLicenseUrl;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getIdNumber() { return idNumber; }
    public String getDniFrontUrl() { return dniFrontUrl; }
    public String getDniBackUrl() { return dniBackUrl; }
    public String getDriverLicenseUrl() { return driverLicenseUrl; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setStatus(String status) { this.status = status; }
}
