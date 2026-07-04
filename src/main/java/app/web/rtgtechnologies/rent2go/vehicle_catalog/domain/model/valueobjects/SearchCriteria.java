package app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.valueobjects;

import app.web.rtgtechnologies.rent2go.shared.domain.ValueObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
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
 * - minYear: Minimum model year (optional)
 * - maxYear: Maximum model year (optional)
 * - seats: Minimum number of seats (optional)
 * - transmission: Transmission type (optional)
 * - fuelType: Fuel type (optional)
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
    private Integer minYear;
    private Integer maxYear;
    private Integer seats;
    private String transmission;
    private String fuelType;
    private String location;
    private Double centerLatitude;
    private Double centerLongitude;
    private Double radiusKm;
    private String featureName;
    private LocalDate startDate;
    private LocalDate endDate;

    // ========== Factory Methods ==========

    /**
     * Create search criteria with price range only.
     */
    public static SearchCriteria byPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        validatePriceRange(minPrice, maxPrice);
        return new SearchCriteria(null, minPrice, maxPrice, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    /**
     * Create search criteria with categories only.
     */
    public static SearchCriteria byCategories(List<String> categories) {
        return new SearchCriteria(categories, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    /**
     * Create search criteria with location only.
     */
    public static SearchCriteria byLocation(String location) {
        return new SearchCriteria(null, null, null, null, null, null, null, null, location, null, null, null, null, null, null);
    }

    /**
     * Create search criteria with geographic radius filter.
     */
    public static SearchCriteria byRadius(Double centerLatitude, Double centerLongitude, Double radiusKm) {
        return new SearchCriteria(null, null, null, null, null, null, null, null, null, centerLatitude, centerLongitude, radiusKm, null, null, null);
    }

    /**
     * Create search criteria with all filters.
     */
    public static SearchCriteria full(
        List<String> categories,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer minYear,
        Integer maxYear,
        Integer seats,
        String transmission,
        String fuelType,
        String location,
        Double centerLatitude,
        Double centerLongitude,
        Double radiusKm,
        String featureName,
        LocalDate startDate,
        LocalDate endDate
    ) {
        if (minPrice != null && maxPrice != null) {
            validatePriceRange(minPrice, maxPrice);
        }
        validateDateRange(startDate, endDate);
        return new SearchCriteria(categories, minPrice, maxPrice, minYear, maxYear, seats, transmission, fuelType, location, centerLatitude, centerLongitude, radiusKm, featureName, startDate, endDate);
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
     * Validate date range: if either bound is provided, both must be provided and startDate <= endDate.
     */
    private static void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return;
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Both startDate and endDate are required for a date-range search");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be <= endDate");
        }
    }

    /**
     * Check if this criteria filters by price.
     */
    public boolean hasPrice() {
        return minPrice != null || maxPrice != null;
    }

    /**
     * Check if this criteria filters by minimum price.
     */
    public boolean hasMinPrice() {
        return minPrice != null;
    }

    /**
     * Check if this criteria filters by maximum price.
     */
    public boolean hasMaxPrice() {
        return maxPrice != null;
    }

    /**
     * Check if this criteria filters by year range.
     */
    public boolean hasYearRange() {
        return minYear != null || maxYear != null;
    }

    /**
     * Check if this criteria filters by seats.
     */
    public boolean hasSeats() {
        return seats != null;
    }

    /**
     * Check if this criteria filters by transmission.
     */
    public boolean hasTransmission() {
        return transmission != null && !transmission.isBlank();
    }

    /**
     * Check if this criteria filters by fuel type.
     */
    public boolean hasFuelType() {
        return fuelType != null && !fuelType.isBlank();
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

    /**
     * Check if this criteria filters by geographic radius.
     * Requires all three: centerLatitude, centerLongitude, and radiusKm.
     */
    public boolean hasRadius() {
        return centerLatitude != null && centerLongitude != null && radiusKm != null;
    }

    /**
     * Check if this criteria filters by a date range (availability-aware search, TS20).
     * Requires both startDate and endDate.
     */
    public boolean hasDateRange() {
        return startDate != null && endDate != null;
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
               Objects.equals(minYear, that.minYear) &&
               Objects.equals(maxYear, that.maxYear) &&
               Objects.equals(seats, that.seats) &&
               Objects.equals(transmission, that.transmission) &&
               Objects.equals(fuelType, that.fuelType) &&
               Objects.equals(location, that.location) &&
               Objects.equals(startDate, that.startDate) &&
               Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categories, minPrice, maxPrice, minYear, maxYear, seats, transmission, fuelType, location, startDate, endDate);
    }

    @Override
    public String toString() {
        return "SearchCriteria{" +
                "categories=" + categories +
                ", minPrice=" + minPrice +
                ", maxPrice=" + maxPrice +
            ", minYear=" + minYear +
            ", maxYear=" + maxYear +
            ", seats=" + seats +
            ", transmission='" + transmission + '\'' +
            ", fuelType='" + fuelType + '\'' +
                ", location='" + location + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
