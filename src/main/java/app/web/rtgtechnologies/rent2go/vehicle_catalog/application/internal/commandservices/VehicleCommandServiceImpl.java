package app.web.rtgtechnologies.rent2go.vehicle_catalog.application.internal.commandservices;

import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.Vehicle;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.VehicleCategory;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.aggregates.VehicleImage;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands.RegisterVehicleCommand;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands.SetPrimaryVehicleImageCommand;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands.RemoveVehicleImageCommand;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands.UpdateVehicleDetailsCommand;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands.UpdateVehiclePricingCommand;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.commands.UploadVehicleImageCommand;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.services.VehicleCommandService;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.domain.model.valueobjects.VehicleStatus;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.infrastructure.persistence.jpa.repositories.VehicleCategoryRepository;
import app.web.rtgtechnologies.rent2go.vehicle_catalog.infrastructure.persistence.jpa.repositories.VehicleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * VehicleCommandServiceImpl
 *
 * Application service implementation for vehicle command handling.
 * Orchestrates between domain logic and persistence layer.
 *
 * Hexagonal Architecture: Command Handler (application layer)
 * CQRS Pattern: Command Handler for RegisterVehicleCommand and UpdateVehiclePricingCommand
 *
 * Returns domain aggregates (not DTOs). DTOs are created by Assemblers in the interfaces layer.
 */
@Service
@AllArgsConstructor
@Transactional
public class VehicleCommandServiceImpl implements VehicleCommandService {

    private final VehicleRepository vehicleRepository;
    private final VehicleCategoryRepository vehicleCategoryRepository;

    /**
     * Handle RegisterVehicleCommand
     *
     * Creates and persists a new vehicle in the catalog.
     *
     * @param command RegisterVehicleCommand with vehicle details
     * @return Created Vehicle aggregate
     * @throws IllegalArgumentException if duplicate license plate or VIN
     * @throws IllegalArgumentException if category not found
     */
    @Override
    public Vehicle handle(RegisterVehicleCommand command) {
        // Validate vehicle doesn't already exist
        if (vehicleRepository.findByLicensePlate(command.licensePlate()).isPresent()) {
            throw new IllegalArgumentException(
                "Vehicle with license plate already exists: " + command.licensePlate()
            );
        }

        if (vehicleRepository.findByVin(command.vin()).isPresent()) {
            throw new IllegalArgumentException(
                "Vehicle with VIN already exists: " + command.vin()
            );
        }

        // Fetch category
        VehicleCategory category = vehicleCategoryRepository.findById(command.categoryId())
            .orElseThrow(() -> {
                var allCategories = vehicleCategoryRepository.findAll();
                var categoryIds = allCategories.stream()
                    .map(cat -> cat.getId() + " (" + cat.getName() + ")")
                    .toList();
                var message = "Category not found: " + command.categoryId() + ". Available categories: " + categoryIds;
                return new IllegalArgumentException(message);
            });

        // Create vehicle aggregate
        Vehicle vehicle = Vehicle.builder()
            .licensePlate(command.licensePlate())
            .make(command.make())
            .model(command.model())
            .year(command.year())
            .vin(command.vin())
            .status(VehicleStatus.AVAILABLE)
            .dailyPrice(command.dailyPrice())
            .category(category)
            .location(command.location())
            .description(command.description())
            .seats(command.seats())
            .transmission(command.transmission())
            .fuelType(command.fuelType())
            .build();

        // Persist and return domain aggregate (not DTO)
        return vehicleRepository.save(vehicle);
    }

    /**
     * Handle UpdateVehiclePricingCommand
     *
     * Updates the daily price of an existing vehicle.
     *
     * @param command UpdateVehiclePricingCommand with vehicle ID and new price
     * @return Updated Vehicle aggregate
     * @throws IllegalArgumentException if vehicle not found
     */
    @Override
    public Vehicle handle(UpdateVehiclePricingCommand command) {
        Vehicle vehicle = vehicleRepository.findById(command.vehicleId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Vehicle not found: " + command.vehicleId()
            ));

        vehicle.updateDailyPrice(command.newDailyPrice());
        return vehicleRepository.save(vehicle);
    }

    /**
     * Handle UpdateVehicleDetailsCommand
     */
    @Override
    public Vehicle handle(UpdateVehicleDetailsCommand command) {
        Vehicle vehicle = vehicleRepository.findById(command.vehicleId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Vehicle not found: " + command.vehicleId()
            ));

        VehicleCategory category = vehicleCategoryRepository.findById(command.categoryId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Category not found: " + command.categoryId()
            ));

        vehicle.updateDetails(
            category,
            command.make(),
            command.model(),
            command.year(),
            command.location(),
            command.description(),
            command.seats(),
            command.transmission(),
            command.fuelType()
        );

        return vehicleRepository.save(vehicle);
    }

    /**
     * Handle UploadVehicleImageCommand
     *
     * Uploads and attaches an image to a vehicle.
     *
     * @param command UploadVehicleImageCommand with vehicle ID and image details
     * @return Updated Vehicle aggregate with new image
     * @throws IllegalArgumentException if vehicle not found
     * @throws IllegalArgumentException if imagePath is empty
     */
    @Override
    public Vehicle handle(UploadVehicleImageCommand command) {
        if (command.imagePath() == null || command.imagePath().isBlank()) {
            throw new IllegalArgumentException("Image path cannot be empty");
        }

        Vehicle vehicle = vehicleRepository.findById(command.vehicleId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Vehicle not found: " + command.vehicleId()
            ));

        // Create new VehicleImage entity
        VehicleImage image = VehicleImage.builder()
            .imagePath(command.imagePath())
            .imageUrl(command.imageUrl())
            .isPrimary(command.isPrimary() != null && command.isPrimary())
            .uploadDate(LocalDateTime.now())
            .build();

        // Add image to vehicle aggregate
        vehicle.addImage(image);

        return vehicleRepository.save(vehicle);
    }

    /**
     * Handle RemoveVehicleImageCommand
     *
     * Removes an image from a vehicle.
     *
     * @param command RemoveVehicleImageCommand with vehicle and image IDs
     * @return Updated Vehicle aggregate with image removed
     * @throws IllegalArgumentException if vehicle not found
     * @throws IllegalArgumentException if image not found in vehicle
     */
    @Override
    public Vehicle handle(RemoveVehicleImageCommand command) {
        Vehicle vehicle = vehicleRepository.findById(command.vehicleId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Vehicle not found: " + command.vehicleId()
            ));

        boolean removed = vehicle.removeImage(command.imageId());
        if (!removed) {
            throw new IllegalArgumentException(
                "Image not found for vehicle: " + command.vehicleId()
            );
        }

        return vehicleRepository.save(vehicle);
    }

    /**
     * Handle SetPrimaryVehicleImageCommand
     */
    @Override
    public Vehicle handle(SetPrimaryVehicleImageCommand command) {
        Vehicle vehicle = vehicleRepository.findByIdWithImages(command.vehicleId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Vehicle not found: " + command.vehicleId()
            ));

        vehicle.setPrimaryImage(command.imageId());
        return vehicleRepository.save(vehicle);
    }
}
