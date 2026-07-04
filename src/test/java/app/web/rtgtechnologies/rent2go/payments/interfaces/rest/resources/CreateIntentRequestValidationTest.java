package app.web.rtgtechnologies.rent2go.payments.interfaces.rest.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Issue 1 root-cause investigation (Kotlin create-intent HTTP 422): reproduces, at the exact
 * Jackson-deserialization + Bean Validation layer Spring uses for {@code @Valid @RequestBody
 * CreateIntentRequest}, the precise JSON body Kotlin's PaymentsApi.kt/CreateIntentRequest
 * produces for POST /api/v1/payments/create-intent, to conclusively prove/disprove a field-level
 * mismatch without needing a live backend + MySQL instance (unavailable in this environment).
 */
class CreateIntentRequestValidationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Validator validator;

    CreateIntentRequestValidationTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    /** Exact JSON body kotlinx.serialization produces for CreateIntentRequest(reservationId=42,
     * amountCents=15000, currency="usd") — field names/types/order as sent by PaymentsApi.kt. */
    private static final String KOTLIN_PAYLOAD =
            "{\"reservationId\":42,\"amountCents\":15000,\"currency\":\"usd\"}";

    @Test
    void kotlinPayload_deserializesAndPassesValidation() throws Exception {
        CreateIntentRequest request = objectMapper.readValue(KOTLIN_PAYLOAD, CreateIntentRequest.class);

        assertEquals(42L, request.getReservationId());
        assertEquals(15000L, request.getAmountCents());
        assertEquals("usd", request.getCurrency());

        Set<ConstraintViolation<CreateIntentRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(),
                "Kotlin's exact wire payload must pass CreateIntentRequest's Bean Validation, found: " + violations);
    }

    /** Confirms the actual failure mode this project's GlobalExceptionHandler maps to 422: only
     * a validation failure on a syntactically well-formed body (not malformed JSON) reaches
     * MethodArgumentNotValidException. A reservationId of 0 (falsy/uninitialized default in a
     * client bug) is the one realistic client-side mistake that reproduces the reported 422. */
    @Test
    void reservationIdZero_failsPositiveValidation_reproducing422() throws Exception {
        CreateIntentRequest request = objectMapper.readValue(
                "{\"reservationId\":0,\"amountCents\":15000,\"currency\":\"usd\"}", CreateIntentRequest.class);

        Set<ConstraintViolation<CreateIntentRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("reservationId", violations.iterator().next().getPropertyPath().toString());
    }

    /** A blank currency string (as opposed to an absent one) also reproduces a 422 — confirms
     * @NotBlank (not just @NotNull) is in play, ruling out a Kotlin default-omission theory. */
    @Test
    void blankCurrency_failsNotBlankValidation_reproducing422() throws Exception {
        CreateIntentRequest request = objectMapper.readValue(
                "{\"reservationId\":42,\"amountCents\":15000,\"currency\":\"\"}", CreateIntentRequest.class);

        Set<ConstraintViolation<CreateIntentRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("currency", violations.iterator().next().getPropertyPath().toString());
    }
}
