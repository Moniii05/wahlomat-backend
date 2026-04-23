package htw.wahlomat.wahlomat.dto;
/**
 * Request DTO for creating or updating a candidate profile.
 *
 * @param firstname candidate first name
 * @param lastname candidate last name
 * @param facultyId faculty identifier
 * @param aboutMe candidate "about me" text
 */
public record CandidateProfileRequest(
        String firstname,
        String lastname,
        Long facultyId,
        String aboutMe
)
{
}
