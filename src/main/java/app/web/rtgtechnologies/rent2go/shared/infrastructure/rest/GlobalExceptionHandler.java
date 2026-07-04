package app.web.rtgtechnologies.rent2go.shared.infrastructure.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Issue 1 (Kotlin create-intent HTTP 422 investigation): this handler previously returned
    // the validation-error detail in the response body but never logged it server-side, so a
    // client-reported 422 could not be correlated with a specific request/field from server logs
    // alone. Every 422 is now logged at WARN with the offending path + field/message pairs.
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", error.getDefaultMessage() == null ? "" : error.getDefaultMessage(),
                        "rejectedValue", String.valueOf(error.getRejectedValue())))
                .collect(Collectors.toList());
        log.warn("422 Unprocessable Entity on {}: {}", request.getDescription(false), errors);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(validationBody(
            "Validation failed",
            errors
        ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        List<Map<String, String>> errors = ex.getConstraintViolations().stream()
                .map(error -> Map.of(
                    "field", error.getPropertyPath().toString(),
                    "message", error.getMessage()
                ))
                .collect(Collectors.toList());
        log.warn("422 Unprocessable Entity (constraint violation): {}", errors);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(validationBody(
            "Validation failed",
            errors
        ));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                         HttpHeaders headers,
                                                                         HttpStatusCode status,
                                                                         WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "Forbidden");
        body.put("message", "You do not have permission to perform this action. This endpoint requires the ADMIN role.");
        body.put("reason", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthentication(AuthenticationException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "Unauthorized");
        body.put("message", "Authentication is required to access this resource.");
        body.put("reason", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "Internal server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private Map<String, Object> validationBody(String message, List<Map<String, String>> errors) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Unprocessable Entity");
        body.put("message", message);
        body.put("errors", errors);
        return body;
    }
}
