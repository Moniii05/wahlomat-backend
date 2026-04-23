package htw.wahlomat.wahlomat.dto;

import htw.wahlomat.wahlomat.model.AnswerOption;
/**
 * Response DTO representing a question together with a candidate's stored answer (if present).
 *
 * @param questionId identifier of the question
 * @param questionText question text
 * @param candidateAnswerId identifier of the stored candidate answer (may be {@code null})
 * @param selectedOption selected option of the stored answer (may be {@code null})
 */
public record QuestionWithAnswerResponse (
        Long questionId,
        String questionText,
        Long candidateAnswerId,
        AnswerOption selectedOption

) {}







