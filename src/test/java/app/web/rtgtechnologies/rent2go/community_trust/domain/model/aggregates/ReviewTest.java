package app.web.rtgtechnologies.rent2go.community_trust.domain.model.aggregates;

import app.web.rtgtechnologies.rent2go.community_trust.domain.model.valueobjects.RatingValue;
import app.web.rtgtechnologies.rent2go.community_trust.domain.model.valueobjects.ReviewCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ReviewTest
 *
 * Sprint 5 (US70/TS21, BRD-2026-07-05-Politica-Aprobacion-Reviews.md, Option A): a newly
 * submitted review must default to APPROVED, not PENDING -- no admin moderation workflow
 * exists or is planned for this project, so a PENDING default provided no real moderation
 * value, only structural rating/review invisibility.
 */
class ReviewTest {

    @Test
    void submit_defaultsToApproved_notPending() {
        Review review = Review.submit(1L, 2L, 3L, 4L, ReviewCategory.VEHICLE, RatingValue.of(5), "Great car");

        assertTrue(review.isApproved());
        assertFalse(review.isRejected());
        assertFalse(review.isFlagged());
    }

    @Test
    void submit_withoutOptionalReviewedUserIdOrComment_stillDefaultsToApproved() {
        Review review = Review.submit(1L, 2L, 3L, null, ReviewCategory.COMMUNICATION, RatingValue.of(3), null);

        assertTrue(review.isApproved());
    }

    @Test
    void submit_throws_whenRequiredFieldsMissing() {
        assertThrows(IllegalArgumentException.class,
                () -> Review.submit(null, 2L, 3L, 4L, ReviewCategory.VEHICLE, RatingValue.of(5), "x"));
        assertThrows(IllegalArgumentException.class,
                () -> Review.submit(1L, 2L, 3L, 4L, null, RatingValue.of(5), "x"));
        assertThrows(IllegalArgumentException.class,
                () -> Review.submit(1L, 2L, 3L, 4L, ReviewCategory.VEHICLE, null, "x"));
    }

    @Test
    void getRatingValue_returnsUnderlyingIntValue() {
        Review review = Review.submit(1L, 2L, 3L, 4L, ReviewCategory.DRIVER, RatingValue.of(4), "Good");

        assertEquals(4, review.getRatingValue());
    }
}
