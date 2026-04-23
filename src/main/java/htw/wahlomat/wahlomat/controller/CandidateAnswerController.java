package htw.wahlomat.wahlomat.controller;

import htw.wahlomat.wahlomat.dto.*;
import htw.wahlomat.wahlomat.exception.UnauthorizedAccessException;
import htw.wahlomat.wahlomat.model.User;
import htw.wahlomat.wahlomat.dto.CandidateAnswerRequest;
import htw.wahlomat.wahlomat.dto.CandidateAnswerResponse;
import htw.wahlomat.wahlomat.dto.MatchingRequest;
import htw.wahlomat.wahlomat.dto.QuestionWithAnswerResponse;
import htw.wahlomat.wahlomat.service.CandidateAnswerService;
import htw.wahlomat.wahlomat.service.MatchingService;
import htw.wahlomat.wahlomat.model.AnswerOption;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller responsible for managing candidate answers and matching.
 *
 * <p>This controller provides endpoints to:</p>
 * <ul>
 *   <li>Retrieve all questions including stored candidate answers</li>
 *   <li>Create or update answers (upsert)</li>
 *   <li>Delete individual answers</li>
 *   <li>Allow administrators to inspect answers per question</li>
 *   <li>Calculate matching results for voters</li>
 * </ul>
 *
 * <p>Base path: {@code /api/candidate-profiles}</p>
 *
 * <p>Authorization rules:</p>
 * <ul>
 *   <li>Candidates may only access their own answers</li>
 *   <li>Administrators may retrieve answers per question</li>
 *   <li>Matching endpoint is publicly accessible</li>
 * </ul>
 */

@RestController
@RequestMapping("/api/candidate-profiles")
@Tag( name = "CandidateAnswer", description = "CandidateAnswer management API")
public class CandidateAnswerController {

    private final Logger logger = LoggerFactory.getLogger(CandidateController.class);
    private final CandidateAnswerService candidateAnswerService;
    private final MatchingService matchingService;

    /**
     * Creates a new controller instance.
     *
     * @param candidateAnswerService service handling answer persistence logic
     * @param matchingService service responsible for matching calculation
     */
    public CandidateAnswerController(CandidateAnswerService candidateAnswerService,
                                     MatchingService matchingService) {
        this.candidateAnswerService = candidateAnswerService;
        this.matchingService = matchingService;
    }

    /**
     * Validates that the currently authenticated user
     * is allowed to access the given user ID.
     *
     * <p>Only candidates are allowed to access their own answers.
     * Access to foreign user data is forbidden.</p>
     *
     * @param requestedUserId ID of the user whose answers are requested
     * @throws UnauthorizedAccessException if access is not permitted
     */
    private void validateUserAccess(Long requestedUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof User currentUser)) {
            throw new UnauthorizedAccessException("Nicht eingeloggt");
        }

        // Prüfe: Ist der eingeloggte User = angefragte userId?
        if (!currentUser.getUserId().equals(requestedUserId)) {
            logger.warn("User {} tried to access answers of user {}",
                    currentUser.getUserId(), requestedUserId);
            throw new UnauthorizedAccessException(
                    "Du darfst nur auf deine eigenen Antworten zugreifen"
            );
        }
    }


    /**
     * Returns all questions including the stored answers
     * for a specific candidate.
     *
     * <p>HTTP: {@code GET /api/candidate-profiles/{userId}/questions}</p>
     *
     * <p>If the request is authenticated, access is validated
     * to ensure users can only view their own answers.</p>
     *
     * @param userId ID of the candidate
     * @return list of {@link QuestionWithAnswerResponse}
     */
    @GetMapping("/{userId}/questions")
    public ResponseEntity<List<QuestionWithAnswerResponse>> getQuestionsWithAnswers(@PathVariable Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Nur prüfen, wenn User WIRKLICH eingeloggt ist
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            validateUserAccess(userId);
        }

        List<QuestionWithAnswerResponse> questions =
                candidateAnswerService.getQuestionsWithAnswersForUser(userId);
        return ResponseEntity.ok(questions);
    }


    /**
     * Returns all candidate answers for a specific question.
     *
     * <p>HTTP: {@code GET /api/candidate-profiles/answers/{questionId}}</p>
     *
     * <p>Access: {@code ROLE_ADMIN} only.</p>
     *
     * @param questionId ID of the question
     * @return list of {@link CandidateAnswerResponse}
     */

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/answers/{questionId}")
    public ResponseEntity<List<CandidateAnswerResponse>> getAnswersByQuestion(
            @PathVariable Long questionId) {
        try{
        List<CandidateAnswerResponse> answers =
                candidateAnswerService.getAnswersByQuestion(questionId);
        return ResponseEntity.ok(answers);
        } catch (Exception e){
            logger.error("Error fetching answers for questionId {}: {}", questionId, e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Creates or updates a candidate answer (upsert).
     *
     * <p>HTTP: {@code PATCH /api/candidate-profiles/{userId}/answers/{questionId}}</p>
     *
     * <p>If an answer already exists, it is updated.
     * Otherwise, a new answer is created.</p>
     *
     * <p>Access: {@code ROLE_CANDIDATE} only.</p>
     *
     * @param userId ID of the candidate
     * @param questionId ID of the question
     * @param request request containing the selected answer option
     * @return updated or created {@link CandidateAnswerResponse}
     */

    @PatchMapping("/{userId}/answers/{questionId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<CandidateAnswerResponse> upsertAnswer(
            @PathVariable Long userId,
            @PathVariable Long questionId,
            @Valid @RequestBody CandidateAnswerRequest request) {

        validateUserAccess(userId);

        //konvertiere int -> Enum
        AnswerOption option = AnswerOption.fromValue(request.selectedOption());

        //Übergebe konvertiertes Enum an Service
        CandidateAnswerResponse response =
                candidateAnswerService.upsertAnswer(userId, questionId, option);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a specific candidate answer.
     *
     * <p>HTTP: {@code DELETE /api/candidate-profiles/{userId}/answers/{answerId}}</p>
     *
     * <p>Access: {@code ROLE_CANDIDATE} only.</p>
     *
     * @param userId ID of the candidate
     * @param answerId ID of the answer to delete
     * @return {@code 204 No Content} if deletion was successful
     */

    @DeleteMapping("/{userId}/answers/{answerId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Void> deleteCandidateAnswer(
            @PathVariable Long userId,
            @PathVariable Long answerId) {
        validateUserAccess(userId);
        candidateAnswerService.deleteAnswer(userId, answerId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Calculates matching results for a specific committee.
     *
     * <p>HTTP: {@code POST /api/candidate-profiles/matching/{committeeId}}</p>
     *
     * <p>The request body contains the voter answers.
     * The service calculates matching percentages for all candidates
     * in the given committee.</p>
     *
     * @param committeeId ID of the committee
     * @param voterAnswers list of voter answers including weighting
     * @return list of {@link CandidateMatchingResult}
     */

    @PostMapping("/matching/{committeeId}")
    public ResponseEntity<List<CandidateMatchingResult>> calculateMatching(
            @PathVariable String committeeId,
            @Valid @RequestBody List<MatchingRequest> voterAnswers
    ) {
        List<CandidateMatchingResult> results = matchingService.calculateForCommittee(committeeId, voterAnswers);
        return ResponseEntity.ok(results);
    }




}