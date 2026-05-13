package app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.valueobjects;

import app.web.rtgtechnologies.rent2go.shared.domain.ValueObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * SearchCriteria Value Object
 * 
 * Encapsulates vehicle search parameters for queries.
 * Makes search logic type-safe and reusable across the domain.
 * 
 * Fields:
 * - categories: List of category names to filter by (e.g., ["Sedan", "SUV"])
 * - minPrice: Minimum daily rental price (optional)
 * - maxPrice: Maximum daily rental price (optional)
 * - location: Geographic location for pickup/dropoff (optional)
 * 
 * Invariants:
 * - minPrice must be <= maxPrice (if both provided)
 * - prices must be >= 0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteria extends ValueObject {

    private List<String> categories;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String location;

    // ========== Factory Methods ==========

    /**
     * Create search criteria with price range only.
     */
    public static SearchCriteria byPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        validatePriceRange(minPrice, maxPrice);
        return new SearchCriteria(null, minPrice, maxPrice, null);
    }

    /**
     * Create search criteria with categories only.
     */
    public static SearchCriteria byCategories(List<String> categories) {
        return new SearchCriteria(categories, null, null, null);
    }

    /**
     * Create search criteria with location only.
     */
    public static SearchCriteria byLocation(String location) {
        return new SearchCriteria(null, null, null, location);
    }

    /**
     * Create search criteria with all filters.
     */
    public static SearchCriteria full(List<String> categories, BigDecimal minPrice, BigDecimal maxPrice, String location) {
        if (minPrice != null && maxPrice != null) {
            validatePriceRange(minPrice, maxPrice);
        }
        return new SearchCriteria(categories, minPrice, maxPrice, location);
    }

    // ========== Validation ==========

    /**
     * Validate price range: minPrice must be <= maxPrice, both must be >= 0.
     */
    private static void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null || maxPrice == null) {
            return;
        }
        if (minPrice.compareTo(BigDecimal.ZERO) < 0 || maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Prices must be non-negative");
        }
        if (minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice must be <= maxPrice");
        }
    }

    /**
     * Check if this criteria filters by price.
     */
    public boolean hasPrice() {
        return minPrice != null && maxPrice != null;
    }

    /**
     * Check if this criteria filters by categories.
     */
    public boolean hasCategories() {
        return categories != null && !categories.isEmpty();
    }

    /**
     * Check if this criteria filters by location.
     */
    public boolean hasLocation() {
        return location != null && !location.isBlank();
    }

    // ========== Value Object Contract ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchCriteria that = (SearchCriteria) o;
        return Objects.equals(categories, that.categories) &&
               Objects.equals(minPrice, that.minPrice) &&
               Objects.equals(maxPrice, that.maxPrice) &&
               Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categories, minPrice, maxPrice, location);
    }

    @Override
    public String toString() {
        return "SearchCriteria{" +
                "categories=" + categories +
                ", minPrice=" + minPrice +
                ", maxPrice=" + maxPrice +
                ", location='" + location + '\'' +
                '}';
    }
}
