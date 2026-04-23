package htw.wahlomat.wahlomat.dto;

import jakarta.validation.constraints.*;

/**
 * Request DTO for creating a new candidacy list.
 *
 * @param number list number (must be >= 1)
 * @param listName display name of the list
 * @param committeeId committee identifier the list belongs to
 */
public record CandidacyListRequest(
        @Min(1) int number,
        String listName,
        String committeeId

)
{
}