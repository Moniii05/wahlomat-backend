package htw.wahlomat.wahlomat.dto;

import htw.wahlomat.wahlomat.model.AnswerOption;
import jakarta.validation.constraints.NotNull;
/**
 * Request DTO for submitting a candidate answer.
 *
 * <p>The frontend sends the selected option as an integer value.
 * It will be converted into {@code AnswerOption} on the server side.</p>
 *
 * @param selectedOption numeric representation of the selected answer option (must not be null)
 */
public record CandidateAnswerRequest (

    @NotNull(message = "Selected option is required")
    int selectedOption //FE sendet Integer
) {}


