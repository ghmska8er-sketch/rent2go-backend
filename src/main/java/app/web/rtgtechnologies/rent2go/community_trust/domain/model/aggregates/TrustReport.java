package app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.valueobjects.TrustReportStatus;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.valueobjects.TrustSubjectType;
import app.web.rtgtechnologies.rent2go.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trust_reports")
@Getter
@NoArgsConstructor
public class TrustReport extends AuditableAbstractAggregateRoot<TrustReport> {

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", nullable = false, length = 20)
    private TrustSubjectType subjectType;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "reported_user_id")
    private Long reportedUserId;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TrustReportStatus status;

    @Column(name = "moderation_note", length = 500)
    private String moderationNote;

    private TrustReport(TrustSubjectType subjectType,
                        Long subjectId,
                        Long reservationId,
                        Long reviewId,
                        Long reportedUserId,
                        Long reporterId,
                        String reason) {
        this.subjectType = subjectType;
        this.subjectId = subjectId;
        this.reservationId = reservationId;
        this.reviewId = reviewId;
        this.reportedUserId = reportedUserId;
        this.reporterId = reporterId;
        this.reason = reason;
        this.status = TrustReportStatus.OPEN;
    }

    public static TrustReport open(TrustSubjectType subjectType,
                                   Long subjectId,
                                   Long reviewId,
                                   Long reportedUserId,
                                   Long reporterId,
                                   String reason) {
        if (subjectType == null || subjectId == null || reporterId == null || reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("subjectType, subjectId, reporterId and reason are required");
        }

        return new TrustReport(subjectType, subjectId, null, reviewId, reportedUserId, reporterId, reason.trim());
    }

    public static TrustReport openDispute(Long reservationId,
                                          Long reportedUserId,
                                          Long reporterId,
                                          String reason) {
        if (reservationId == null || reporterId == null || reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reservationId, reporterId and reason are required");
        }

        return new TrustReport(
            TrustSubjectType.RESERVATION,
            reservationId,
            reservationId,
            null,
            reportedUserId,
            reporterId,
            reason.trim()
        );
    }

    public void resolve(String note) {
        this.status = TrustReportStatus.RESOLVED;
        this.moderationNote = note;
    }

    public void dismiss(String note) {
        this.status = TrustReportStatus.DISMISSED;
        this.moderationNote = note;
    }
}