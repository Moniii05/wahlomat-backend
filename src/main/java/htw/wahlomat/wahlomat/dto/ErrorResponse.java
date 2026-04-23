package htw.wahlomat.wahlomat.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized error response DTO for REST APIs.
 *
 * <p>This DTO is returned by exception handling (e.g. authentication errors,
 * validation errors, unexpected server errors) to provide a consistent error
 * format for the frontend.</p>
 *
 * <p>Optional fields {@code details} and {@code validationErrors} are only
 * included in the JSON response if they are not {@code null}.</p>
 *
 * @param timestamp time when the error response was generated
 * @param status HTTP status code (e.g. 400, 401, 403, 404, 500)
 * @param error short error label (e.g. "Unauthorized", "Forbidden", "Validation Failed")
 * @param message human-readable error message
 * @param path request path that caused the error
 * @param details optional technical details (e.g. stack trace information in development)
 * @param validationErrors optional field-level validation errors (field -> message)
 */
public record ErrorResponse(
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,

        // Zusätzliche Infos
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String details,  // Stack trace oder zusätzliche Info

        @JsonInclude(JsonInclude.Include.NON_NULL)
        Map<String, String> validationErrors  // Für Validierung
) {
    /**
     * Creates a simple error response without details or validation errors.
     *
     * @param status HTTP status code
     * @param error short error label
     * @param message human-readable error message
     * @param path request path
     * @return created {@link ErrorResponse}
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                null,
                null
        );
    }

    /**
     * Creates an error response including technical details.
     *
     * @param status HTTP status code
     * @param error short error label
     * @param message human-readable error message
     * @param path request path
     * @param details technical details (e.g. stack trace text)
     * @return created {@link ErrorResponse}
     */
    public static ErrorResponse withDetails(int status, String error, String message,
                                            String path, String details) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                details,
                null
        );
    }

    /**
     * Creates an error response for validation errors.
     *
     * @param status HTTP status code
     * @param message human-readable message
     * @param path request path
     * @param validationErrors map of validation errors (field -> message)
     * @return created {@link ErrorResponse}
     */
    public static ErrorResponse withValidationErrors(int status, String message,
                                                     String path,
                                                     Map<String, String> validationErrors) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                "Validation Failed",
                message,
                path,
                null,
                validationErrors
        );
    }
}