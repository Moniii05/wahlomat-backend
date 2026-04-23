package htw.wahlomat.wahlomat.dto;

/**
 * Request DTO for creating or updating a candidacy.
 *
 * @param committeeId committee identifier for which the candidacy is created
 * @param listId identifier of the selected candidacy list
 */
public record CandidacyRequest( 
    String committeeId, // FRS
    Long listId // referenziert ListDto, welche Liste wurde gewählt
    ) {}