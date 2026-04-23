package htw.wahlomat.wahlomat.dto;

/**
 * Response DTO representing a candidacy.
 *
 * @param candidacyId unique identifier of the candidacy
 * @param committeeId committee identifier of the candidacy
 * @param listId identifier of the selected candidacy list
 */
public record CandidacyResponse(
    Long candidacyId,
    String committeeId,
    Long listId
) {}