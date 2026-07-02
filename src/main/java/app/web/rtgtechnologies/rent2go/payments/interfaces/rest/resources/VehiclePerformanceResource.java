package app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * VehiclePerformanceResource
 *
 * Response DTO for the per-vehicle performance endpoint (US24).
 * Exposes reservation count, revenue sum, and occupancy percentage
 * for a single vehicle within an optional date range.
 */
public class VehiclePerformanceResource {

    private Long vehicleId;
    private String from;
    private String to;
    private int reservationCount;
    private BigDecimal totalRevenue;
    private String currency;
    private double occupancyPercentage;

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public int getReservationCount() { return reservationCount; }
    public void setReservationCount(int reservationCount) { this.reservationCount = reservationCount; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public double getOccupancyPercentage() { return occupancyPercentage; }
    public void setOccupancyPercentage(double occupancyPercentage) { this.occupancyPercentage = occupancyPercentage; }
}
