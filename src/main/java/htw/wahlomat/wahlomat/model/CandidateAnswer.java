package htw.wahlomat.wahlomat.model;

import htw.wahlomat.wahlomat.model.profilePage.CandidateProfile;
import jakarta.persistence.*;

/**
 * Entity representing the answer of a candidate to a specific question.
 *
 * <p>Each candidate can answer each question exactly once.
 * This is enforced by a unique constraint on (user_id, question_id).</p>
 *
 * <p>The selected answer is stored as an {@link AnswerOption} enum value.</p>
 */
@Entity
@Table(name = "candidate_answers", //
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_question",  // unique constraint, verhindert nur neue duplicate. nicht das Updaten bestehender Antworten!
                columnNames = {"userId", "questionId"}
        )
)

public class CandidateAnswer {

    /**
     * Primary key (surrogate key).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long candidateAnswerId;  // surrogate key


    /**
     * The candidate profile who gave this answer.
     * Many answers can belong to one candidate.
     */
    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)     //todo: multilayer validation
    private CandidateProfile candidate;  //FK

    /**
     * The question that was answered.
     * Many answers can reference the same question.
     */
    @ManyToOne
    @JoinColumn(name="question_id", nullable = false)
    private Question question;  //FK

    /**
     * The selected answer option.
     * Stored as String representation of the enum.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnswerOption selectedOption;


    /**
     * Default constructor required by JPA.
     */
    public CandidateAnswer() {}

    /**
     * Creates a new CandidateAnswer.
     *
     * @param candidate      the candidate profile
     * @param question       the answered question
     * @param selectedOption selected answer option
     */
    public CandidateAnswer(CandidateProfile candidate, Question question, AnswerOption selectedOption) {
        this.candidate = candidate;
        this.question = question;
        this.selectedOption = selectedOption;
    }

    /** @return unique ID of the candidate answer */
    public Long getCandidateAnswerId() {
        return candidateAnswerId;
    }

    public void setCandidateAnswerId(Long candidateAnswerId) {
        this.candidateAnswerId = candidateAnswerId;
    }

    /** @return candidate profile */
    public CandidateProfile getCandidate() {
        return candidate;
    }

    public void setCandidate(CandidateProfile candidate) {
        this.candidate = candidate;
    }

    /** @return answered question */
    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question questionId) {
        this.question = questionId;
    }

    /** @return selected answer option */
    public AnswerOption getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(AnswerOption selectedOption) {
        this.selectedOption = selectedOption;
    }
}
