package htw.wahlomat.wahlomat.service;

import htw.wahlomat.wahlomat.dto.CandidateAnswerResponse;
import htw.wahlomat.wahlomat.dto.QuestionWithAnswerResponse;
import htw.wahlomat.wahlomat.model.AnswerOption;
import htw.wahlomat.wahlomat.model.CandidateAnswer;
import htw.wahlomat.wahlomat.model.Question;
import htw.wahlomat.wahlomat.model.User;
import htw.wahlomat.wahlomat.model.profilePage.CandidateProfile;
import htw.wahlomat.wahlomat.repository.CandidateAnswerRepository;
import htw.wahlomat.wahlomat.repository.QuestionRepository;
import htw.wahlomat.wahlomat.repository.UserRepository;
import htw.wahlomat.wahlomat.repository.profilePage.CandidateProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for managing candidate answers to questions.
 *
 * <p>This service provides:
 * <ul>
 *   <li>Retrieval of all questions including a candidate's stored answers</li>
 *   <li>Retrieval of all answers for a single question (admin overview)</li>
 *   <li>Upsert logic (create or update) for one answer per candidate per question</li>
 *   <li>Deletion of a single candidate answer</li>
 * </ul>
 *
 * <p>Business rules:
 * <ul>
 *   <li>Candidates must not choose {@link AnswerOption#FRAGE_UEBERSPRINGEN}.</li>
 *   <li>For each candidate and question there is at most one stored answer.</li>
 * </ul>
 */
@Service
public class CandidateAnswerService {

    private final CandidateAnswerRepository candidateAnswerRepository;
    private final QuestionRepository questionRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new {@code CandidateAnswerService}.
     *
     * @param candidateAnswerRepository repository for persisting and querying candidate answers
     * @param questionRepository repository for accessing questions/statements
     * @param candidateProfileRepository repository for resolving candidate profiles for users
     * @param userRepository repository for resolving users by ID
     */
    public CandidateAnswerService(CandidateAnswerRepository candidateAnswerRepository,
                                   QuestionRepository questionRepository,
                                   CandidateProfileRepository candidateProfileRepository,
                                   UserRepository userRepository) {
        this.candidateProfileRepository = candidateProfileRepository;
        this.candidateAnswerRepository = candidateAnswerRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Returns all questions along with the stored answer of the given user (if present).
     *
     * <p>The result contains one entry per question. If the user has not answered a question yet,
     * {@code candidateAnswerId} and {@code selectedOption} are {@code null}.
     *
     * @param userId ID of the user whose answers should be merged with all questions
     * @return list of {@link QuestionWithAnswerResponse} (one per question)
     * @throws RuntimeException if the user does not exist or no {@link CandidateProfile} exists for the user
     */
    public List<QuestionWithAnswerResponse> getQuestionsWithAnswersForUser(Long userId) {
        // User finden
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // CandidateProfile finden
        CandidateProfile candidate = candidateProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Candidate profile not found for user ID: " + userId));

        //  Alle Fragen laden
        List<Question> allQuestions = questionRepository.findAll();

        // Alle Antworten des kandidaten laden
        List<CandidateAnswer> userAnswers = candidateAnswerRepository.findByCandidate(candidate);

        // Antworten in Map umwandeln für schnellen Lookup (question → Answer)
        Map<Long, CandidateAnswer> answerMap = userAnswers.stream()
                .collect(Collectors.toMap(
                        answer -> answer.getQuestion().getQuestionId(),
                        answer -> answer
                ));

        // Fragen + Antworten kombinieren
        return allQuestions.stream()
                .map(question -> {
                    CandidateAnswer answer = answerMap.get(question.getQuestionId());
                    return new QuestionWithAnswerResponse(
                            question.getQuestionId(),
                            question.getQuestion(),
                            answer != null ? answer.getCandidateAnswerId() : null,
                            answer != null ? answer.getSelectedOption() : null
                    );
                })
                .collect(Collectors.toList());
    }


    /**
     * Returns all candidate answers for a specific question.
     *
     * <p>Used for an admin overview (e.g. to see how candidates answered a statement).
     *
     * @param questionId ID of the question
     * @return list of {@link CandidateAnswerResponse} for the given question
     * @throws RuntimeException if the question does not exist
     */
    public List<CandidateAnswerResponse> getAnswersByQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found for question ID: " + questionId));
        List<CandidateAnswer> answers = candidateAnswerRepository.findByQuestion(question);
        return answers.stream()
                .map(this::mapToAnswerResponse)
                .collect(Collectors.toList());
    }

    /**
     * Creates or updates a candidate answer for a given user and question.
     *
     * <p>Upsert logic:
     * <ul>
     *   <li>If an answer for (candidate, question) already exists, it is updated.</li>
     *   <li>If no answer exists, a new one is created.</li>
     * </ul>
     *
     * <p>Business rule: candidates are not allowed to choose
     * {@link AnswerOption#FRAGE_UEBERSPRINGEN}.
     *
     * @param userId ID of the user (candidate)
     * @param questionId ID of the question
     * @param selectedOption selected answer option
     * @return the saved answer as {@link CandidateAnswerResponse}
     * @throws IllegalArgumentException if {@code selectedOption} is {@link AnswerOption#FRAGE_UEBERSPRINGEN}
     * @throws RuntimeException if the user, candidate profile or question does not exist
     */
    @Transactional
    public CandidateAnswerResponse upsertAnswer(Long userId, Long questionId, AnswerOption selectedOption) {

        validateCandidateAnswer(selectedOption); //kandidaten dürfen nicht überspringen

        // User finden
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // CandidateProfile finden
        CandidateProfile candidate = candidateProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Candidate profile not found for user ID: " + userId));

        // Question finden
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found for question ID: " + questionId));

        // 1. Suche existierende Antwort
        CandidateAnswer answer = candidateAnswerRepository
                .findByCandidateAndQuestion(candidate, question)
                .map(existing -> {
                    // ═══ UPDATE: Antwort existiert bereits ═══
                    existing.setSelectedOption(selectedOption);
                    return existing;
                })
                .orElseGet(() -> {
                    // ═══ CREATE: Neue Antwort ═══
                    CandidateAnswer newAnswer = new CandidateAnswer();
                    newAnswer.setCandidate(candidate);
                    newAnswer.setQuestion(question);
                    newAnswer.setSelectedOption(selectedOption);
                    return newAnswer;
                });

        // 2. Speichern (INSERT oder UPDATE)
        CandidateAnswer saved = candidateAnswerRepository.save(answer);

        // 3. Response zurückgeben
        return mapToAnswerResponse(saved);
    }


    /**
     * Deletes a single candidate answer by its ID.
     *
     * <p>Security rule: the answer must belong to the given user, otherwise an exception is thrown.
     *
     * @param userId ID of the user requesting the deletion
     * @param answerId ID of the answer to delete
     * @throws IllegalArgumentException if the answer does not exist
     * @throws SecurityException if the answer does not belong to the given user
     */
    @Transactional
    public void deleteAnswer(Long userId, Long answerId) {
        CandidateAnswer answer = candidateAnswerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found: " + answerId));

        // Sicherheit: Prüfen ob Antwort zum User gehört
        if (!answer.getCandidate().getId().equals(userId)) {  //oder .getUser().getId()?
            throw new SecurityException("Answer " + answerId + " does not belong to user " + userId);
        }
        candidateAnswerRepository.delete(answer);
    }

    /**
     * Validates candidate answer rules before persisting.
     *
     * <p>Candidates must answer every question and may not use
     * {@link AnswerOption#FRAGE_UEBERSPRINGEN}.
     *
     * @param option selected option to validate
     * @throws IllegalArgumentException if the option is {@link AnswerOption#FRAGE_UEBERSPRINGEN}
     */
    private void validateCandidateAnswer(AnswerOption option) {
        if (option == AnswerOption.FRAGE_UEBERSPRINGEN) {
            throw new IllegalArgumentException(
                    "Kandidaten müssen alle Fragen beantworten. " +
                            "Die Option 'Überspringen' steht nicht zur Verfügung."
            );
        }
    }




    // ========== helper: MAPPING METHODEN ==========
    /**
     * Maps a {@link CandidateAnswer} entity to a {@link CandidateAnswerResponse} DTO.
     *
     * @param entity persisted answer entity
     * @return DTO containing answer ID, question ID and selected option
     */
    private CandidateAnswerResponse mapToAnswerResponse(CandidateAnswer entity) {
        return new CandidateAnswerResponse(
                entity.getCandidateAnswerId(),
                entity.getQuestion().getQuestionId(),
                entity.getSelectedOption()
        );
    }


}
