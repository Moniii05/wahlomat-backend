package htw.wahlomat.wahlomat.dto;

import htw.wahlomat.wahlomat.model.AnswerOption;
import jakarta.validation.constraints.NotNull;
/**
 * Request DTO representing a single voter answer used for matching.
 *
 * @param questionId identifier of the question
 * @param selectedOption selected answer option
 * @param isWeighted indicates whether this question is weighted by the voter
 */
public record MatchingRequest(
        @NotNull Long questionId,
        @NotNull AnswerOption selectedOption,
        @NotNull Boolean isWeighted
)
{}
