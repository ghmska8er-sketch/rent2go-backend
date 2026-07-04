package app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.valueobjects;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TS20 — SearchCriteria's new optional startDate/endDate fields: hasDateRange() detection and
 * the both-or-neither validation rule.
 */
class SearchCriteriaTest {

    @Test
    void hasDateRangeIsFalseWhenNeitherDateProvided() {
        SearchCriteria criteria = SearchCriteria.full(
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        );

        assertFalse(criteria.hasDateRange());
    }

    @Test
    void hasDateRangeIsTrueWhenBothDatesProvided() {
        SearchCriteria criteria = SearchCriteria.full(
            null, null, null, null, null, null, null, null, null, null, null, null, null,
            LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5)
        );

        assertTrue(criteria.hasDateRange());
    }

    @Test
    void rejectsOnlyStartDateWithoutEndDate() {
        assertThrows(IllegalArgumentException.class, () -> SearchCriteria.full(
            null, null, null, null, null, null, null, null, null, null, null, null, null,
            LocalDate.of(2026, 8, 1), null
        ));
    }

    @Test
    void rejectsStartDateAfterEndDate() {
        assertThrows(IllegalArgumentException.class, () -> SearchCriteria.full(
            null, null, null, null, null, null, null, null, null, null, null, null, null,
            LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 5)
        ));
    }

    @Test
    void searchWithoutDateRangeBehavesAsBeforeRegressionCheck() {
        SearchCriteria criteria = SearchCriteria.byPriceRange(java.math.BigDecimal.TEN, java.math.BigDecimal.valueOf(100));

        assertFalse(criteria.hasDateRange());
        assertTrue(criteria.hasPrice());
    }
}
