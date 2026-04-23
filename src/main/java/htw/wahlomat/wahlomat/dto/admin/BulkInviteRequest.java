package htw.wahlomat.wahlomat.dto.admin;

import jakarta.validation.constraints.NotNull;
import java.util.List;
/**
 * Request DTO for bulk candidate invitations.
 *
 * <p>Contains a list of email addresses that should receive
 * an invitation to register as candidate.</p>
 *
 * @param emails list of candidate email addresses (must not be null)
 */
public record BulkInviteRequest (
    @NotNull List<String> emails 
) {}
