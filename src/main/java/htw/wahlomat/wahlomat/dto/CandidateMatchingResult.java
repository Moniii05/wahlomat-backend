package htw.wahlomat.wahlomat.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Result DTO representing the matching outcome for a single candidate.
 *
 * <p>Contains candidate metadata and list/committee information as well as
 * the calculated matching percentage.</p>
 *
 * @param candidateId unique identifier of the candidate
 * @param firstname candidate first name
 * @param lastname candidate last name
 * @param aboutMe candidate description text
 * @param facultyId faculty identifier of the candidate
 * @param listId identifier of the candidacy list
 * @param listNumber number of the candidacy list
 * @param listName display name of the candidacy list
 * @param committeeId committee identifier
 * @param matchingPercentage calculated match value in percent (0..100)
 */
public record CandidateMatchingResult(
        Long candidateId,
        String firstname,
        String lastname,
        String aboutMe,
        Long facultyId,

        Long listId,
        Integer listNumber,
        String listName,
        
        String committeeId,

        @Min(0)
        @Max(100)
        double matchingPercentage
){
}
