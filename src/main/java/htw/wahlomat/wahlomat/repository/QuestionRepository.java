package htw.wahlomat.wahlomat.repository;

import htw.wahlomat.wahlomat.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for accessing {@link Question} entities.
 *
 * <p>Provides standard CRUD operations via {@link JpaRepository}
 * and custom query methods for application-specific lookups.
 */
public interface QuestionRepository extends JpaRepository<Question, Long> {
    // für Controller Tests
    /**
     * Finds a question entity by its question text.
     *
     * <p>This method is typically used in controller or service tests
     * to verify the existence of a specific question.
     *
     * @param questionText the exact question text
     * @return the {@link Question} entity if found, otherwise {@code null}
     */
    Question findByQuestion(String questionText);
}
