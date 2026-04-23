package htw.wahlomat.wahlomat.dto.admin;

import jakarta.validation.constraints.*;

/**
 * Request DTO for inviting a single candidate.
 *
 * <p>Used by administrators to invite a user
 * via their email address.</p>
 *
 * @param email HTW email address of the candidate
 */
public record InviteCandidateRequest(
    @NotBlank @Email String email
) {}
