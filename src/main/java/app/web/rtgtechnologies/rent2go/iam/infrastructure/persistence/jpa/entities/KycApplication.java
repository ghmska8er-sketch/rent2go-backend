package app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "kyc_applications")
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

    @Column(name = "document_url")
    private String documentUrl;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected KycApplication() {}

    public KycApplication(Long userId, String fullName, String idNumber, String documentUrl, String status, Instant createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.idNumber = idNumber;
        this.documentUrl = documentUrl;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getIdNumber() { return idNumber; }
    public String getDocumentUrl() { return documentUrl; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setStatus(String status) { this.status = status; }
}
