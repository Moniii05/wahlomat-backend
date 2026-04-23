package htw.wahlomat.wahlomat.repository;

import htw.wahlomat.wahlomat.model.CandidateAnswer;
import htw.wahlomat.wahlomat.model.Question;
import htw.wahlomat.wahlomat.model.profilePage.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link CandidateAnswer} entities.
 *
 * <p>Handles persistence and retrieval of candidate answers
 * related to questions in the Wahl-O-Mat system.
 */
@Repository
public interface CandidateAnswerRepository extends JpaRepository<CandidateAnswer, Long> {
    // Finde alle Antworten eines spezifischen Kandidaten
    /**
     * Retrieves all answers submitted by a specific candidate.
     *
     * @param candidate the candidate profile
     * @return list of answers belonging to the candidate
     */
    List<CandidateAnswer> findByCandidate(CandidateProfile candidate);

    // Finde eine spezifische Antwort (User + Frage)
    /**
     * Retrieves a specific answer of a candidate for a given question.
     *
     * <p>Ensures uniqueness per (candidate, question) combination.
     *
     * @param candidate the candidate profile
     * @param question the question entity
     * @return optional containing the answer if present
     */
    Optional<CandidateAnswer> findByCandidateAndQuestion(CandidateProfile candidate, Question question);

    // Finde alle Antworten zu einer spezifischen Frage
    /**
     * Retrieves all answers associated with a specific question.
     *
     * @param question the question entity
     * @return list of answers for the question
     */
    List<CandidateAnswer> findByQuestion(Question question);

    // US5 Kandidatenerstellung
    /**
     * Deletes all answers of a specific candidate.
     *
     * @param candidate the candidate profile
     */
    void deleteByCandidate(CandidateProfile candidate);

    /**
     * Deletes all answers related to a specific question.
     *
     * <p>Executed within a transaction to ensure data consistency.
     *
     * @param question the question entity
     */
    @Transactional
    void deleteByQuestion(Question question); // nur 1 DB query
}