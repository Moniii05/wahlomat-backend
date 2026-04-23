package htw.wahlomat.wahlomat.controller;


import htw.wahlomat.wahlomat.dto.AuthResponse;
import htw.wahlomat.wahlomat.dto.ChangePasswordRequest;
import htw.wahlomat.wahlomat.dto.LoginRequest;
import htw.wahlomat.wahlomat.dto.RegisterRequest;
import htw.wahlomat.wahlomat.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * REST controller responsible for user authentication.
 *
 * <p>This controller provides public endpoints for:</p>
 * <ul>
 *   <li>Candidate registration</li>
 *   <li>User login (ADMIN and CANDIDATE)</li>
 *   <li>Password change for authenticated users</li>
 * </ul>
 *
 * <p>Base path: {@code /api/auth}</p>
 *
 * <p>Error handling is delegated to the global exception handler.</p>
 */

@RestController
@RequestMapping("/api/auth")
@Tag( name = "Authentication", description = "User authentication API")
public class AuthController {

    private final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    /**
     * Creates a new authentication controller.
     *
     * @param authService service responsible for authentication logic
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new candidate.
     *
     * <p>HTTP: {@code POST /api/auth/register}</p>
     *
     * <p>Only invited candidates with a valid HTW email address
     * can complete registration.</p>
     *
     * @param request registration request containing email, password,
     *                firstname, lastname and faculty
     * @return {@link AuthResponse} containing JWT token and user role
     */

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Exception Handling passiert jetzt in GlobalExceptionHandler!
        AuthResponse response = authService.register(request);
        logger.info("Candidate registered successfully: {}", request.email());
        return ResponseEntity
                .status(HttpStatus.CREATED) // 201 bei erfolgreicher Registrierung
                .body(response);

    }



    /**
     * Authenticates a user (ADMIN or CANDIDATE).
     *
     * <p>HTTP: {@code POST /api/auth/login}</p>
     *
     * @param request login request containing email and password
     * @return {@link AuthResponse} containing JWT token and user role
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        logger.info("User logged in successfully: {} (Role: {})",
                request.email(), response.role());
        return ResponseEntity.ok(response);
    }


    /**
     * Changes the password of the currently authenticated user.
     *
     * <p>HTTP: {@code PUT /api/auth/change-password}</p>
     *
     * @param request request containing the current password and new password
     * @return {@code 200 OK} if password change was successful,
     *         {@code 400 Bad Request} if validation fails
     */
    @PutMapping("/change-password")
    public ResponseEntity changePassword(@RequestBody ChangePasswordRequest request)
    {
        try {
            authService.changePassword(request);
            logger.info("Password changed successfully");
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            logger.error("Password change failed: {}",
                    e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

}





