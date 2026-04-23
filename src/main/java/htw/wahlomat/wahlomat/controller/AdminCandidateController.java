package htw.wahlomat.wahlomat.controller;

import htw.wahlomat.wahlomat.dto.admin.InviteCandidateRequest;
import htw.wahlomat.wahlomat.dto.admin.InviteCandidateResponse;
import htw.wahlomat.wahlomat.dto.admin.RegisteredCandidateResponse;
import htw.wahlomat.wahlomat.model.User;
import htw.wahlomat.wahlomat.repository.UserRepository;
import htw.wahlomat.wahlomat.service.AdminCandidateService;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

import htw.wahlomat.wahlomat.dto.admin.BulkInviteRequest;
import htw.wahlomat.wahlomat.dto.admin.BulkInviteResponse;

/**
 * REST controller for managing candidates by administrators.
 *
 * <p>All endpoints in this controller are restricted to users with role {@code ADMIN}.
 * The restriction is enforced using {@link PreAuthorize}.</p>
 *
 * <p>Provides functionality for:</p>
 * <ul>
 *   <li>Inviting single or multiple candidates</li>
 *   <li>Listing invited and registered candidates</li>
 *   <li>Deleting individual or all registered candidates</li>
 * </ul>
 *
 * <p>Base path: {@code /api/admin/candidates}</p>
 */
@RestController
@RequestMapping("/api/admin/candidates")
@PreAuthorize("hasRole('ADMIN')") //gesamter Controller nur für admin!
public class AdminCandidateController {

    private final AdminCandidateService adminCandidateService;
    private final UserRepository userRepository;

    /**
     * Creates a new controller instance.
     *
     * @param adminCandidateService service layer handling candidate administration logic
     * @param userRepository repository used to look up users before deletion
     */
    public AdminCandidateController(AdminCandidateService adminCandidateService,
                                    UserRepository userRepository) {
        this.userRepository = userRepository;
        this.adminCandidateService = adminCandidateService;
    }

    /**
     * Invites a single candidate by email.
     *
     * <p>HTTP: {@code POST /api/admin/candidates}</p>
     *
     * @param request request containing the candidate's email address
     * @return {@link InviteCandidateResponse} with invitation details
     */
    @PostMapping                                                  // RB = JSON mapping 
    public ResponseEntity<InviteCandidateResponse> invite(@Valid @RequestBody InviteCandidateRequest request) {
        InviteCandidateResponse response = adminCandidateService.invite(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        // 201 + body
    }

    /**
     * Invites multiple candidates at once.
     *
     * <p>HTTP: {@code POST /api/admin/candidates/bulk}</p>
     *
     * @param request request containing a list of email addresses
     * @return {@link BulkInviteResponse} summarizing successful and failed invitations
     */
    @PostMapping("/bulk")
    public ResponseEntity<BulkInviteResponse> bulkInvite(@Valid @RequestBody BulkInviteRequest request) {
        BulkInviteResponse response = adminCandidateService.bulkInvite(request.emails());
         return ResponseEntity.ok(response);
     }



    /**
     * Deletes a single registered candidate.
     *
     * <p>HTTP: {@code DELETE /api/admin/candidates/{userId}}</p>
     *
     * @param userId ID of the candidate user to delete
     * @return {@code 204 No Content} if successful,
     *         {@code 404 Not Found} if user does not exist
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if(user.isEmpty()) {
            return ResponseEntity.notFound().build();  // 404
        }
        else
        {
            adminCandidateService.deleteCandidate(user.get());
            return ResponseEntity.noContent().build(); // 204
        }
    }

    /**
     * Deletes all registered candidates (reset operation).
     *
     * <p>HTTP: {@code DELETE /api/admin/candidates}</p>
     *
     * @return {@code 204 No Content} after deletion
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAllRegistered() {
        adminCandidateService.deleteAllRegistered();
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns all registered candidates.
     *
     * <p>HTTP: {@code GET /api/admin/candidates}</p>
     *
     * @return list of {@link RegisteredCandidateResponse}
     */
    @GetMapping   
    public List<RegisteredCandidateResponse> listRegistered() {
         return adminCandidateService.listRegistered();
         // 200 + autom. JSON 
    }

    /**
     * Returns all candidate invitations sorted alphabetically.
     *
     * <p>HTTP: {@code GET /api/admin/candidates/invites}</p>
     *
     * @return list of {@link InviteCandidateResponse}
     */
    @GetMapping("/invites")
    public List<InviteCandidateResponse> listInvites() {
         return adminCandidateService.listInvites();
}

}

