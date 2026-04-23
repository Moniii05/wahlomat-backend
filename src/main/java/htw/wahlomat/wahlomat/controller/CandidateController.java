package htw.wahlomat.wahlomat.controller;
import htw.wahlomat.wahlomat.exception.UnauthorizedAccessException;
import htw.wahlomat.wahlomat.model.User;
import htw.wahlomat.wahlomat.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import htw.wahlomat.wahlomat.dto.CandidateProfileRequest;
import htw.wahlomat.wahlomat.dto.CandidateProfileResponse;
import htw.wahlomat.wahlomat.model.Role;
import htw.wahlomat.wahlomat.service.CandidateService;
import java.util.Optional;

/**
 * REST controller for managing candidate profiles.
 *
 * <p>This controller provides endpoints to:</p>
 * <ul>
 *   <li>Retrieve a candidate profile</li>
 *   <li>Create or update a candidate profile</li>
 * </ul>
 *
 * <p>Base path: {@code /api/candidate-profiles}</p>
 *
 * <p>Authorization rules:</p>
 * <ul>
 *   <li>Candidates may access and modify only their own profile</li>
 *   <li>Administrators may access and modify any profile</li>
 * </ul>
 */

@RestController    // diese klasse beantwortet web anfragen + leifert JSON
@RequestMapping("/api/candidate-profiles") // basispfad für alle Methoden
@Tag( name = "Candidate", description = "CandidateProfile management API")
public class CandidateController {

  private final Logger logger = LoggerFactory.getLogger(CandidateController.class);
  private final CandidateService service;
  private final UserRepository userRepository;
    /**
     * Creates a new candidate controller.
     *
     * @param service service layer handling profile logic
     * @param userRepository repository for retrieving user entities
     */

    public CandidateController(CandidateService service, UserRepository userRepository) {
    this.service = service;
    this.userRepository = userRepository;
  }

    /**
     * Validates that the currently authenticated user
     * is allowed to access the given user ID.
     *
     * <p>Rules:</p>
     * <ul>
     *   <li>ADMIN users may access all profiles</li>
     *   <li>CANDIDATE users may access only their own profile</li>
     * </ul>
     *
     * @param requestedUserId ID of the requested profile
     * @throws UnauthorizedAccessException if access is not permitted
     */

    private void validateUserAccess(Long requestedUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // DEBUG LOGS
        logger.info("=== validateUserAccess DEBUG ===");
        logger.info("Requested userId: {}", requestedUserId);
        logger.info("Authentication object: {}", auth);
        logger.info("Authentication class: {}", auth != null ? auth.getClass().getName() : "null");

        if (auth == null) {
            logger.error("Authentication is NULL!");
            throw new UnauthorizedAccessException("Nicht eingeloggt");
        }

        logger.info("Principal object: {}", auth.getPrincipal());
        logger.info("Principal class: {}", auth.getPrincipal().getClass().getName());

        if (!(auth.getPrincipal() instanceof User)) {
            logger.error("Principal is NOT a User instance! It's: {}", auth.getPrincipal().getClass());
            throw new UnauthorizedAccessException("Nicht eingeloggt");
        }

        User currentUser = (User) auth.getPrincipal();

        logger.info("Current user ID: {}", currentUser.getUserId());
        logger.info("Current user role: {}", currentUser.getRole());
        logger.info("Current user email: {}", currentUser.getEmail());

        // Admin darf ALLES - early return
        if (currentUser.getRole() == Role.ADMIN) {
            logger.debug("Admin {} accessing profile of user {}",
                    currentUser.getUserId(), requestedUserId);
            return;  // Admin hat Zugriff auf alle Profile
        }

        // Candidate darf NUR eigenes Profil
        if (!currentUser.getUserId().equals(requestedUserId)) {
            logger.warn("User {} tried to access profile of user {}",
                    currentUser.getUserId(), requestedUserId);
            throw new UnauthorizedAccessException(
                    "Du darfst nur auf dein eigenes Profil zugreifen"
            );
        }

        logger.info("Access granted for user {} to their own profile", currentUser.getUserId());
    }


    /**
     * Returns the profile of a specific user.
     *
     * <p>HTTP: {@code GET /api/candidate-profiles/{userId}}</p>
     *
     * <p>Access:</p>
     * <ul>
     *   <li>CANDIDATE: own profile only</li>
     *   <li>ADMIN: any profile</li>
     * </ul>
     *
     * @param userId ID of the user whose profile is requested
     * @return {@link CandidateProfileResponse} if found,
     *         {@code 404 Not Found} if user does not exist
     */

    @GetMapping ("/{userId}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    // profil lesen --> CandidateProfileDto zurückgeben
    public ResponseEntity<CandidateProfileResponse> getProfile(@PathVariable Long userId) {
        validateUserAccess(userId);  //Sicherheitsprüfung

        Optional<User> optionalUser = this.userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
        else {
            User user = optionalUser.get();
            CandidateProfileResponse response =  service.getProfile(user);
            return ResponseEntity.ok(response);
        }

    }

    /**
     * Creates or updates a candidate profile.
     *
     * <p>HTTP: {@code PUT /api/candidate-profiles/{userId}}</p>
     *
     * <p>Access:</p>
     * <ul>
     *   <li>CANDIDATE: own profile only</li>
     *   <li>ADMIN: any profile</li>
     * </ul>
     *
     * @param profileRequestDto profile data to be stored
     * @param userId ID of the user whose profile is modified
     * @return {@code 200 OK} if successful,
     *         {@code 404 Not Found} if user does not exist
     */

    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<Void> saveProfile(@Valid @RequestBody CandidateProfileRequest profileRequestDto, @PathVariable Long userId)
    {
        validateUserAccess(userId);  // Sicherheitsprüfung: Darf User auf diese userId zugreifen?

        Optional<User> optionalUser = this.userRepository.findById(userId);
        if (optionalUser.isEmpty())
        {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
        else
        {
            User user = optionalUser.get();
            service.saveProfile(user, profileRequestDto);
            return ResponseEntity.status(HttpStatus.OK)
                    .build();
        }
    }

}

