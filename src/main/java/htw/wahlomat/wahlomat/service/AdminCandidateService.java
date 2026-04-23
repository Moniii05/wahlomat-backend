package htw.wahlomat.wahlomat.service;

import htw.wahlomat.wahlomat.dto.admin.BulkInviteResponse;
import htw.wahlomat.wahlomat.dto.admin.InviteCandidateRequest;
import htw.wahlomat.wahlomat.dto.admin.InviteCandidateResponse;
import htw.wahlomat.wahlomat.dto.admin.RegisteredCandidateResponse;
import htw.wahlomat.wahlomat.model.User;

import java.util.List;
/**
 * Service interface for administrative candidate management.
 *
 * <p>Provides business operations for:
 * <ul>
 *     <li>Inviting candidates</li>
 *     <li>Managing registered candidates</li>
 *     <li>Bulk invitation handling</li>
 * </ul>
 *
 * This service is intended to be used exclusively by administrators.
 */
public interface AdminCandidateService {

    /**
     * Creates or renews an invitation for a candidate.
     *
     * <p>If an invitation for the given email already exists,
     * it may be updated (e.g. renewed if expired).
     *
     * @param req request containing the candidate email
     * @return information about the created or updated invitation
     */
    InviteCandidateResponse invite(InviteCandidateRequest req);

    /**
     * Deletes a specific registered candidate including
     * associated profile data, candidacies and answers.
     *
     * @param user the candidate user entity to delete
     */
    void deleteCandidate(User user);          // post, 204 No Content

    /**
     * Deletes all registered candidates.
     *
     * <p>This operation performs a complete reset of
     * all candidate-related data.
     */
    void deleteAllRegistered();

    /**
     * Returns a list of all existing candidate invitations.
     *
     * @return list of invitation responses
     */
    List<InviteCandidateResponse> listInvites();

    /**
     * Returns a list of all registered candidates.
     *
     * @return list of registered candidate summaries
     */
    List<RegisteredCandidateResponse> listRegistered(); // anzeigen kandidaten

    /**
     * Processes multiple candidate invitations at once.
     *
     * <p>Duplicate emails are handled internally.
     * Invalid or already processed invitations are skipped.
     *
     * @param emails list of candidate email addresses
     * @return detailed bulk invite result containing
     *         success, skipped and failed entries
     */
    BulkInviteResponse bulkInvite(List<String> emails);
    // anzeige 
    //List<QuestionWithAnswerResponse> listQuestionsWithAnswers(long userId);
}
