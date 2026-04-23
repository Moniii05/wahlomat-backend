package htw.wahlomat.wahlomat.dto.admin;

import htw.wahlomat.wahlomat.model.admin.InviteStatus;
import java.time.Instant;

/**
 * Response DTO representing an invitation entry.
 *
 * <p>Contains metadata about the invitation status
 * and registration state.</p>
 *
 * @param id unique identifier of the invitation
 * @param email invited email address
 * @param status current invitation status
 * @param invitedAt timestamp when the invitation was sent
 * @param registeredUserId ID of the registered user if registration is completed,
 *                         otherwise {@code null}
 */
public record InviteCandidateResponse(
  Long id,
  String email,
  InviteStatus status,
  Instant invitedAt, // für 3 monate logik (-> invite läuft ab)
  Long registeredUserId
) {}
