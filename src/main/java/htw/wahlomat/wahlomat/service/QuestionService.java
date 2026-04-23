package htw.wahlomat.wahlomat.service;


import htw.wahlomat.wahlomat.model.CandidateAnswer;
import htw.wahlomat.wahlomat.model.Question;
import htw.wahlomat.wahlomat.repository.CandidateAnswerRepository;
import htw.wahlomat.wahlomat.repository.QuestionRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service responsible for managing {@link Question} entities.
 *
 * <p>This service provides operations to:
 * <ul>
 *     <li>Load all questions</li>
 *     <li>Create a new question</li>
 *     <li>Delete a question including all dependent {@link CandidateAnswer} entries</li>
 * </ul>
 *
 * <p>Deletion ensures referential integrity by removing all related candidate answers
 * before deleting the question itself.
 */
@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final CandidateAnswerRepository candidateAnswerRepository;


    /**
     * Creates a new {@link QuestionService}.
     *
     * @param questionRepository repository for question persistence
     * @param candidateAnswerRepository repository for candidate answers (used for cascading deletion)
     */
    public QuestionService(QuestionRepository questionRepository,
                           CandidateAnswerRepository candidateAnswerRepository) {
        this.candidateAnswerRepository = candidateAnswerRepository;
        this.questionRepository = questionRepository;
    }

    // Aufruf der findAll Methode des JPARepository um alle Einträge abzurufen
    /**
     * Returns all questions stored in the database.
     *
     * @return list of all {@link Question} entities
     */
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    //Methode zum Anlagen einer neuen Frage in der Datenbank
    /**
     * Creates and persists a new question.
     *
     * <p>If a question with the same text already exists (unique constraint),
     * an {@link IllegalArgumentException} is thrown.
     *
     * @param question the text of the question to create
     * @return the persisted {@link Question} entity
     * @throws IllegalArgumentException if a question with identical text already exists
     */
    public Question createQuestion(String question) {
        try {
            Question newQuestion = new Question(question);
            return questionRepository.save(newQuestion);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Eine Frage mit diesem Text existiert bereits: " + question);
        }
    }

    // Methode zum Löschen einer Frage anhand der Frage-ID
    /**
     * Deletes a question by its ID.
     *
     * <p>Workflow:
     * <ol>
     *     <li>Verify that the question exists</li>
     *     <li>Delete all {@link CandidateAnswer} entries referencing this question</li>
     *     <li>Delete the question itself</li>
     * </ol>
     *
     * <p>The method is transactional to guarantee consistency.
     *
     * @param questionId the ID of the question to delete
     * @throws RuntimeException if the question does not exist
     */
    @Transactional
    public void deleteQuestionById(Long questionId)
    {
        //Zuerst wird geprüft, ob die Aussage mit dieser questionId existiert
        Question toBeDeleted = this.questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question with Id " + questionId + " does not exist"));


        // Abfrage, ob Antworten zu der Aussage existieren + löschen dieser Antworten
        List<CandidateAnswer> existingAnswers = this.candidateAnswerRepository.findByQuestion(toBeDeleted);
        System.out.println("Found " + existingAnswers.size() + " answers for question " + questionId);

        if (!existingAnswers.isEmpty()) {
            this.candidateAnswerRepository.deleteAll(existingAnswers);  // Batch-Löschen
        }

        //jetzt wird Aussage final anhand der Id gelöscht
        this.questionRepository.deleteById(questionId);
    }

}
