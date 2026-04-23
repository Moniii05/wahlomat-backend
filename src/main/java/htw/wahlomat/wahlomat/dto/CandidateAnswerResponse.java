package htw.wahlomat.wahlomat.dto;

import htw.wahlomat.wahlomat.model.AnswerOption; //

/**
 * Response DTO representing a stored candidate answer.
 *
 * @param candidateAnswerId unique identifier of the stored answer
 * @param questionId identifier of the associated question
 * @param selectedOption selected answer option
 */
public record CandidateAnswerResponse (
     Long candidateAnswerId,
     Long questionId,
     AnswerOption selectedOption
) {}
