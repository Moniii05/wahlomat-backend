package htw.wahlomat.wahlomat.controller;

import java.util.List;
import htw.wahlomat.wahlomat.exception.UnauthorizedAccessException;
import htw.wahlomat.wahlomat.model.CandidacyList;
import htw.wahlomat.wahlomat.model.User;
import htw.wahlomat.wahlomat.model.Role;
import htw.wahlomat.wahlomat.model.profilePage.Candidacy;
import htw.wahlomat.wahlomat.repository.CandidacyListRepository;
import htw.wahlomat.wahlomat.repository.UserRepository;
import htw.wahlomat.wahlomat.repository.profilePage.CandidacyRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import htw.wahlomat.wahlomat.dto.CandidacyRequest;
import htw.wahlomat.wahlomat.dto.CandidacyResponse;
import htw.wahlomat.wahlomat.service.CandidateService;

/**
 * REST controller for managing candidacies (candidate applications for committees/lists).
 *
 * <p>This controller provides endpoints to:</p>
 * <ul>
 *   <li>List candidacies of a specific user</li>
 *   <li>List candidacies belonging to a specific candidacy list</li>
 *   <li>Create, update and delete candidacies</li>
 * </ul>
 *
 * <h2>Access control</h2>
 * <ul>
 *   <li>Admins may access and modify candidacies of all users.</li>
 *   <li>Candidates may only access and modify their own candidacies.</li>
 * </ul>
 *
 * <p>Base path: {@code /api/candidacies}</p>
 */
@RestController
@RequestMapping("/api/candidacies")
@Tag( name = "Candidacies", description = "Kandidaturen management API")
public class CandidacyController {

    private final Logger logger = LoggerFactory.getLogger(CandidateController.class);
    private final CandidateService service;
    private final UserRepository userRepository;
    private final CandidacyListRepository listRepository;
    private final CandidacyRepository candidacyRepository;

    /**
     * Creates a new candidacy controller.
     *
     * @param service service layer providing candidacy business logic
     * @param userRepository repository for user lookup
     * @param listRepository repository for candidacy list lookup
     * @param candidacyRepository repository used for ownership checks on update/delete
     */
    public CandidacyController(CandidateService service,
                               UserRepository userRepository,
                               CandidacyListRepository listRepository,
                               CandidacyRepository candidacyRepository) {
        this.service = service;
        this.userRepository = userRepository;
        this.listRepository = listRepository;
        this.candidacyRepository = candidacyRepository;
    }


    /**
     * Validates whether the currently authenticated user is allowed to access the
     * requested user's candidacies.
     *
     * <p>Rules:</p>
     * <ul>
     *   <li>ADMIN: may access candidacies of any user</li>
     *   <li>CANDIDATE: may only access own candidacies</li>
     * </ul>
     *
     * @param requestedUserId user ID whose candidacies are being accessed
     * @throws UnauthorizedAccessException if the user is not authenticated or tries to access foreign data
     */
    private void validateUserAccess(Long requestedUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof User currentUser)) {
            throw new UnauthorizedAccessException("Nicht eingeloggt");
        }

        // Admin darf ALLES - early return
        if (currentUser.getRole() == Role.ADMIN) {
            logger.debug("Admin {} accessing candidacies of user {}",
                    currentUser.getUserId(), requestedUserId);
            return;
        }

        // Candidate darf NUR eigenes Profil
        if (!currentUser.getUserId().equals(requestedUserId)) {
            logger.warn("Candidate {} tried to access candidacies of user {}",
                    currentUser.getUserId(), requestedUserId);
            throw new UnauthorizedAccessException(
                    "Du darfst nur auf deine eigenen Kandidaturen zugreifen"
            );
        }
    }

    /**
     * Returns all candidacies of a given user.
     *
     * <p>HTTP: {@code GET /api/candidacies/user/{userId}}</p>
     *
     * <p>Access: {@code ADMIN} or {@code CANDIDATE}. Candidates may only request their own userId.</p>
     *
     * @param userId ID of the user whose candidacies are requested
     * @return list of {@link CandidacyResponse}
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<List<CandidacyResponse>> getCandidaciesByUser(@PathVariable(name="userId") Long userId) {
        validateUserAccess(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User nicht gefunden"));

        List<CandidacyResponse> candidacies = service.listCandidacies(user);
        return ResponseEntity.ok(candidacies);
    }

    /**
     * Returns all candidacies associated with a specific candidacy list.
     *
     * <p>HTTP: {@code GET /api/candidacies/list/{listId}}</p>
     *
     * <p>This endpoint is public (used by voters to view candidates of a list).</p>
     *
     * @param listId ID of the {@link CandidacyList}
     * @return list of {@link CandidacyResponse}
     */
    @GetMapping("/list/{listId}")
    public ResponseEntity<List<CandidacyResponse>> getCandidaciesByListId(@PathVariable("listId") Long listId)
    {
        CandidacyList list = listRepository.findById(listId)
                .orElseThrow(() -> new UsernameNotFoundException("Liste nicht gefunden"));

        List<CandidacyResponse> candidacies = service.getCandidaciesByCandidacyList(list);
        return ResponseEntity.ok(candidacies);
    }

     /** Creates a new candidacy for a user.
            *
            * <p>HTTP: {@code POST /api/candidacies/user/{userId}}</p>
            *
            * <p>Access: {@code ADMIN} or {@code CANDIDATE}. Candidates may only create candidacies for themselves.</p>
            *
            * @param userId user ID for which the candidacy should be created
     * @param candidacyRequestDto request body containing committee and list selection
     * @return created candidacy as {@link CandidacyResponse} with HTTP status {@code 201 Created}
     */
    @PostMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<CandidacyResponse> create(@PathVariable(name="userId") Long userId, @Valid @RequestBody CandidacyRequest candidacyRequestDto) {

        validateUserAccess(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User nicht gefunden"));

        CandidacyResponse response = service.createCandidacy(user, candidacyRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing candidacy.
     *
     * <p>HTTP: {@code PUT /api/candidacies/{candidacyId}}</p>
     *
     * <p>Access: {@code ADMIN} or {@code CANDIDATE}. Ownership is checked based on the candidacy's user.</p>
     *
     * @param candidacyIdPath ID of the candidacy to update
     * @param candidacyRequestDto request body containing updated committee/list selection
     * @return {@code 200 OK} if update was successful
     */
    @PutMapping("/{candidacyId}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<Void> update(@PathVariable("candidacyId") Long candidacyIdPath,
                       @Valid @RequestBody CandidacyRequest candidacyRequestDto) {
        // Hole Candidacy um Owner zu prüfen
        Candidacy candidacy = candidacyRepository.findById(candidacyIdPath)
                .orElseThrow(() -> new UsernameNotFoundException("Kandidatur nicht gefunden"));

        validateUserAccess(candidacy.getUser().getUserId());

        service.updateCandidacy(candidacyIdPath, candidacyRequestDto);
        return ResponseEntity.ok().build();
    }

    /**
     * Deletes an existing candidacy.
     *
     * <p>HTTP: {@code DELETE /api/candidacies/{candidacyId}}</p>
     *
     * <p>Access: {@code ADMIN} or {@code CANDIDATE}. Ownership is checked based on the candidacy's user.</p>
     *
     * @param candidacyId ID of the candidacy to delete
     * @return {@code 204 No Content} if deletion was successful
     */
    @DeleteMapping("/{candidacyId}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long candidacyId) {
        // Hole Candidacy um Owner zu prüfen
        Candidacy candidacy = candidacyRepository.findById(candidacyId)
                .orElseThrow(() -> new UsernameNotFoundException("Kandidatur nicht gefunden"));

        validateUserAccess(candidacy.getUser().getUserId());

        service.deleteCandidacy(candidacyId);
        return ResponseEntity.noContent().build();
    }
}
