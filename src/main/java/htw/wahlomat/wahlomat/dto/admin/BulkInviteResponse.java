package htw.wahlomat.wahlomat.dto.admin;

import java.util.List;
/**
 * Response DTO for bulk invitation processing.
 *
 * <p>Provides a summary of the invitation attempt including
 * successfully sent, skipped, and failed invitations.</p>
 *
 * @param totalRequested total number of emails received in the request
 * @param totalUnique number of unique email addresses processed
 * @param sent list of successfully sent invitations
 * @param skipped list of skipped invitations (e.g. duplicates)
 * @param failed list of failed invitations with error details
 */
public record BulkInviteResponse(
    int totalRequested,
    int totalUnique,
    List<Result> sent,
    List<Result> skipped,
    List<Result> failed
) {
    public record Result(String email, String status, String reason) {}
}