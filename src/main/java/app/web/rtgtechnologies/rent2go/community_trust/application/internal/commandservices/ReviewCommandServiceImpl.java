package app.web.rtgtechnologies.rent2go.community_trust.application.internal.commandservices;

import app.web.rtgtechnologies.rent2go.booking_reservations.infrastructure.persistence.jpa.repositories.ReservationRepository;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.Review;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.TrustReport;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates.UserReputation;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.ApproveReviewCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.BlockUserForTrustCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.FlagReviewCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.OpenReservationDisputeCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.RejectReviewCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.commands.SubmitReviewCommand;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.services.ReviewCommandService;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.valueobjects.RatingValue;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.valueobjects.ReviewCategory;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.valueobjects.TrustSubjectType;
import app.web.rtgtechnologies.rent2go.community_trust.infrastructure.persistence.jpa.repositories.ReviewRepository;
import app.web.rtgtechnologies.rent2go.community_trust.infrastructure.persistence.jpa.repositories.TrustReportRepository;
import app.web.rtgtechnologies.rent2go.community_trust.infrastructure.persistence.jpa.repositories.UserReputationRepository;
import app.web.rtgtechnologies.rent2go.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewCommandServiceImpl implements ReviewCommandService {

    private final ReviewRepository reviewRepository;
    private final UserReputationRepository userReputationRepository;
    private final TrustReportRepository trustReportRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    @Override
    public Review handle(SubmitReviewCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command is required");
        }

        var reservation = reservationRepository.findById(command.reservationId())
            .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + command.reservationId()));

        if (!reservation.getStatus().isCompleted()) {
            throw new IllegalStateException("Only completed reservations can receive reviews");
        }

        if (!reservation.getVehicleId().equals(command.vehicleId())) {
            throw new IllegalArgumentException("Vehicle does not match the reservation");
        }

        if (!reservation.getRenterId().equals(command.reviewerId()) && !reservation.getOwnerId().equals(command.reviewerId())) {
            throw new IllegalArgumentException("Reviewer must be part of the reservation");
        }

        if (command.category() != ReviewCategory.VEHICLE && command.reviewedUserId() == null) {
            throw new IllegalArgumentException("reviewedUserId is required for non-vehicle reviews");
        }

        if (reservation.getRenterId().equals(command.reviewerId()) && command.reviewedUserId() != null && !reservation.getOwnerId().equals(command.reviewedUserId())) {
            throw new IllegalArgumentException("Renter reviews must target the reservation owner");
        }

        if (reservation.getOwnerId().equals(command.reviewerId()) && command.reviewedUserId() != null && !reservation.getRenterId().equals(command.reviewedUserId())) {
            throw new IllegalArgumentException("Owner reviews must target the reservation renter");
        }

        if (reviewRepository.existsByReservationIdAndReviewerIdAndCategory(
            command.reservationId(),
            command.reviewerId(),
            command.category()
        )) {
            throw new IllegalStateException("A review already exists for this reservation and category");
        }

        var review = Review.submit(
            command.reservationId(),
            command.vehicleId(),
            command.reviewerId(),
            command.reviewedUserId(),
            command.category(),
            RatingValue.of(command.rating()),
            command.comment()
        );

        var saved = reviewRepository.save(review);
        refreshReputation(command.reviewedUserId());
        return saved;
    }

    @Override
    public Review handle(FlagReviewCommand command) {
        var review = reviewRepository.findById(command.reviewId())
            .orElseThrow(() -> new IllegalArgumentException("Review not found: " + command.reviewId()));

        review.flag(command.reason());
        var saved = reviewRepository.save(review);

        trustReportRepository.save(TrustReport.open(
            TrustSubjectType.REVIEW,
            review.getId(),
            review.getId(),
            review.getReviewedUserId(),
            command.reporterId(),
            command.reason()
        ));

        refreshReputation(review.getReviewedUserId());
        return saved;
    }

    @Override
    public Review handle(ApproveReviewCommand command) {
        var review = reviewRepository.findById(command.reviewId())
            .orElseThrow(() -> new IllegalArgumentException("Review not found: " + command.reviewId()));

        review.approve(command.reason());
        var saved = reviewRepository.save(review);
        trustReportRepository.findAllByReviewId(review.getId())
            .forEach(report -> report.resolve(command.reason()));
        refreshReputation(review.getReviewedUserId());
        return saved;
    }

    @Override
    public Review handle(RejectReviewCommand command) {
        var review = reviewRepository.findById(command.reviewId())
            .orElseThrow(() -> new IllegalArgumentException("Review not found: " + command.reviewId()));

        review.reject(command.reason());
        var saved = reviewRepository.save(review);
        trustReportRepository.findAllByReviewId(review.getId())
            .forEach(report -> report.dismiss(command.reason()));
        refreshReputation(review.getReviewedUserId());
        return saved;
    }

    @Override
    public TrustReport handle(OpenReservationDisputeCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command is required");
        }

        var reservation = reservationRepository.findById(command.reservationId())
            .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + command.reservationId()));

        // Bugfix: this previously required the reservation to be COMPLETED,
        // but the client's "Reportar un problema" button (report_issue_screen.dart /
        // DisputeDialog.kt) is reachable from any reservation detail screen,
        // including ACTIVE ones (e.g. reporting vehicle condition or payment
        // issues during an ongoing rental) — not just after it finished. That
        // mismatch made every non-COMPLETED submission fail with an unhandled
        // IllegalStateException (surfaced to the client as a generic 500).
        // Disputes now require the rental to have actually started (handoff
        // done or later) so there is something concrete to report, without
        // forcing the reporter to wait until the whole rental is over.
        if (!reservation.getStatus().isActiveOrLater()) {
            throw new IllegalStateException("Disputes can only be opened once the rental has started");
        }

        if (!reservation.getRenterId().equals(command.reporterId()) && !reservation.getOwnerId().equals(command.reporterId())) {
            throw new IllegalArgumentException("Reporter must be part of the reservation");
        }

        Long reportedUserId = reservation.getRenterId().equals(command.reporterId())
            ? reservation.getOwnerId()
            : reservation.getRenterId();

        return trustReportRepository.save(TrustReport.openDispute(
            reservation.getId(),
            reportedUserId,
            command.reporterId(),
            command.reason()
        ));
    }

    @Override
    public void handle(BlockUserForTrustCommand command) {
        var user = userRepository.findById(command.userId())
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + command.userId()));

        user.block();
        userRepository.save(user);

        var reputation = userReputationRepository.findByUserId(command.userId())
            .orElseGet(() -> UserReputation.forUser(command.userId()));
        reputation.block(command.reason());
        userReputationRepository.save(reputation);

        trustReportRepository.save(TrustReport.open(
            TrustSubjectType.USER,
            command.userId(),
            null,
            command.userId(),
            command.moderatorId(),
            command.reason()
        ));
    }

    private void refreshReputation(Long reviewedUserId) {
        if (reviewedUserId == null) {
            return;
        }

        var reviews = reviewRepository.findAllByReviewedUserId(reviewedUserId);
        var reputation = userReputationRepository.findByUserId(reviewedUserId)
            .orElseGet(() -> UserReputation.forUser(reviewedUserId));
        reputation.recalculate(reviews);
        userReputationRepository.save(reputation);
    }
}