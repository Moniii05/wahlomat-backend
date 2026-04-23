package htw.wahlomat.wahlomat.exception;

import htw.wahlomat.wahlomat.dto.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException; //! nicht die von java security - für digitale Signaturen allgemein

// Spring Security
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

// Spring Web
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Jakarta (Servlet)
import jakarta.servlet.http.HttpServletRequest;

//Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /**
     * Behandelt UnauthorizedAccessException
     * (z.B. Candidate versucht auf fremdes Profil zuzugreifen)
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(
            UnauthorizedAccessException ex,
            HttpServletRequest request) {

        logger.warn("Unauthorized access attempt: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                403,
                "Forbidden",
                ex.getMessage(),  // ← Die richtige Nachricht!
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorResponse);
    }

    /**
     * Behandelt abgelaufene JWT Tokens
     */
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwt(
            ExpiredJwtException ex,
            HttpServletRequest request) {

        logger.warn("JWT Token expired: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Token Expired",
                "Deine Session ist abgelaufen. Bitte melde dich erneut an.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }


    /**
     * Behandelt ungültige JWT Tokens
     * Invalid JWT Token (Malformed, Wrong Signature)
     */

    @ExceptionHandler({MalformedJwtException.class, SignatureException.class})
    public ResponseEntity<ErrorResponse> handleInvalidJwt(
            Exception ex,
            HttpServletRequest request) {

        logger.error("Invalid JWT Token: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid Token",
                "Ungültiger Token. Bitte melde dich erneut an.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Behandelt Spring Security Access Denied
     * (z.B. CANDIDATE versucht Admin-Endpoint aufzurufen)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";

        logger.warn("Access denied for user '{}' to path: {}", username, request.getRequestURI());

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                "Access Denied",
                "Du hast keine Berechtigung für diese Aktion.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Behandelt UsernameNotFoundException (z.B. falsches Login)
     */

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UsernameNotFoundException ex,
            HttpServletRequest request) {

        logger.warn("User not found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication Failed",
                "Ungültige Anmeldedaten.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Behandelt Validierungsfehler (@Valid in Controller)
     */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        logger.warn("Validation failed: {}", errors);

        ErrorResponse error = ErrorResponse.withValidationErrors(
                HttpStatus.BAD_REQUEST.value(),
                "Eingabedaten sind ungültig.",
                request.getRequestURI(),
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Fallback für alle anderen Exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        logger.error("Unexpected error: ", ex);

        // In Production: Keine Details zeigen
        String details = "dev".equals(activeProfile) ? ex.getMessage() : null;

        ErrorResponse error = ErrorResponse.withDetails(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Ein unerwarteter Fehler ist aufgetreten.",
                request.getRequestURI(),
                details
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
