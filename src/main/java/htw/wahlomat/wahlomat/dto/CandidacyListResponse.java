package htw.wahlomat.wahlomat.dto;
/**
 * Response DTO representing a candidacy list.
 *
 * @param listId unique identifier of the list
 * @param number list number
 * @param listName display name of the list
 * @param committeeId committee identifier the list belongs to
 */
public record CandidacyListResponse(
        Long listId,
        int number,
        String listName,
        String committeeId

) {}
