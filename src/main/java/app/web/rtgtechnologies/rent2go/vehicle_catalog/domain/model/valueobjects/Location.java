package app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.valueobjects;

import app.web.rtgtechnologies.rent2go.shared.domain.ValueObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Location Value Object
 * 
 * Represents a geographic location for vehicle pickup/dropoff.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Location extends ValueObject {

    private String name;
    private Double latitude;
    private Double longitude;
    private String city;
    private String country;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(name, location.name) &&
               Objects.equals(city, location.city) &&
               Objects.equals(country, location.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, city, country);
    }
}
