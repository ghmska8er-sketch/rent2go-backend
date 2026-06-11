package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates that at least one of imagePath or imageUrl is provided.
 */
public class ImagePathOrUrlValidator implements ConstraintValidator<ValidImageSource, UploadVehicleImageResource> {

    @Override
    public boolean isValid(UploadVehicleImageResource resource, ConstraintValidatorContext context) {
        if (resource == null) {
            return true;
        }
        boolean hasPath = resource.getImagePath() != null && !resource.getImagePath().isBlank();
        boolean hasUrl = resource.getImageUrl() != null && !resource.getImageUrl().isBlank();
        if (!hasPath && !hasUrl) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("At least one of imagePath or imageUrl must be provided")
                .addPropertyNode("imageUrl")
                .addConstraintViolation();
            return false;
        }
        return true;
    }
}
