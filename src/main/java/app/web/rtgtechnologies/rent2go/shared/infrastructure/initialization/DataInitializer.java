package app.web.rtgtechnologies.rent2go.shared.infrastructure.initialization;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.VehicleCategory;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.infrastructure.persistence.jpa.repositories.VehicleCategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * DataInitializer
 *
 * Spring Boot Component that initializes default data in the database on application startup.
 * This component implements CommandLineRunner to execute initialization logic after the application
 * context is fully loaded.
 *
 * Hexagonal Architecture: Infrastructure initialization component for seeding default data.
 *
 * @author Rent2Go Development Team
 * @since 1.0
 */
@Component
@AllArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final VehicleCategoryRepository vehicleCategoryRepository;

    /**
     * Initialize default vehicle categories on application startup.
     *
     * This method is invoked by Spring Boot after all beans are initialized.
     * It checks if categories already exist to avoid duplicates on application restarts.
     *
     * @param args command line arguments
     * @throws Exception if initialization fails
     */
    @Override
    public void run(String... args) throws Exception {
        initializeVehicleCategories();
    }

    /**
     * Initialize default vehicle categories
     *
     * Populates the vehicle_categories table with standard category types used in vehicle rental.
     * Only inserts if categories don't already exist (idempotent operation).
     */
    private void initializeVehicleCategories() {
        // Check if categories already exist
        if (vehicleCategoryRepository.count() > 0) {
            return; // Categories already initialized, skip
        }

        // Create default vehicle categories
        VehicleCategory sedan = VehicleCategory.builder()
            .name("Sedan")
            .description("Standard 4-door passenger vehicle")
            .iconUrl("https://cdn.example.com/icons/sedan.png")
            .build();

        VehicleCategory suv = VehicleCategory.builder()
            .name("SUV")
            .description("Sport Utility Vehicle with spacious cargo")
            .iconUrl("https://cdn.example.com/icons/suv.png")
            .build();

        VehicleCategory truck = VehicleCategory.builder()
            .name("Truck")
            .description("Heavy-duty pickup or cargo vehicle")
            .iconUrl("https://cdn.example.com/icons/truck.png")
            .build();

        VehicleCategory coupe = VehicleCategory.builder()
            .name("Coupe")
            .description("High-performance 2-door sports car")
            .iconUrl("https://cdn.example.com/icons/coupe.png")
            .build();

        VehicleCategory van = VehicleCategory.builder()
            .name("Van")
            .description("Spacious vehicle for multiple passengers")
            .iconUrl("https://cdn.example.com/icons/van.png")
            .build();

        VehicleCategory hatchback = VehicleCategory.builder()
            .name("Hatchback")
            .description("Compact car with rear cargo access")
            .iconUrl("https://cdn.example.com/icons/hatchback.png")
            .build();

        // Save all categories to the repository
        vehicleCategoryRepository.save(sedan);
        vehicleCategoryRepository.save(suv);
        vehicleCategoryRepository.save(truck);
        vehicleCategoryRepository.save(coupe);
        vehicleCategoryRepository.save(van);
        vehicleCategoryRepository.save(hatchback);
    }
}
