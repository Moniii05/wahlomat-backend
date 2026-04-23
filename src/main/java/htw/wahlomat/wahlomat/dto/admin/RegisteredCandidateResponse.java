package htw.wahlomat.wahlomat.dto.admin;

/**
 * Response DTO representing a registered candidate.
 *
 * <p>Used by administrators to retrieve a list
 * of registered candidates.</p>
 *
 * @param userId unique identifier of the user
 * @param email email address of the candidate
 * @param firstName first name of the candidate
 * @param lastName last name of the candidate
 * @param facultyId identifier of the associated faculty
 */
public record RegisteredCandidateResponse (
    Long userId,
    String email,
    String firstName,
    String lastName,
    Long facultyId
) {}
