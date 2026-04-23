package htw.wahlomat.wahlomat.model;

import jakarta.persistence.*;
/**
 * Entity representing a political statement (question)
 * used in the Wahl-O-Mat matching process.
 *
 * <p>Each question contains a unique text that candidates
 * must answer and voters can evaluate.</p>
 *
 * <p>The question text is unique in the database.</p>
 */
@Entity
@Table(name = "questions")
public class Question {

    /**
     * Primary key (surrogate key).
     */
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)  // ← Strategy hinzufügen!
    @Column(name = "question_id")  // ← Explizit den Spaltennamen angeben!
    Long questionId;

    /**
     * The statement text shown to candidates and voters.
     * Must be unique and not null.
     */
    @Column(name = "question", unique = true, nullable = false)
    String question;

    /**
     * Default constructor required by JPA.
     */
    public Question() {}

    /**
     * Creates a new Question entity.
     *
     * @param question the statement text
     */
    public Question(String question) {
        this.question = question;
    }

    /**
     * @return unique ID of the question
     */
    public Long getQuestionId() {
        return this.questionId;
    }

    /**
     * @return statement text
     */
    public String getQuestion() {
        return this.question;
    }

    /**
     * Updates the question text.
     *
     * @param question new statement text
     */
    public void setQuestion(String question) {
        this.question = question;
    }
}
