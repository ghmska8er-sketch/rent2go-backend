package app.web.rtgtechnologies.rent2go.vehicle_catalog.interfaces.rest.resources;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates that at least one of imagePath or imageUrl is provided.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ImagePathOrUrlValidator.class)
public @interface ValidImageSource {
    String message() default "At least one of imagePath or imageUrl must be provided";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
