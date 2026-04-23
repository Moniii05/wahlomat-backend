package htw.wahlomat.wahlomat.controller;


import htw.wahlomat.wahlomat.model.Question;
import htw.wahlomat.wahlomat.service.QuestionService;
import htw.wahlomat.wahlomat.repository.QuestionRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing statement questions.
 *
 * <p>This controller provides endpoints to:</p>
 * <ul>
 *   <li>Retrieve all questions</li>
 *   <li>Retrieve a single question by ID</li>
 *   <li>Create new questions (ADMIN only)</li>
 *   <li>Delete questions (ADMIN only)</li>
 * </ul>
 *
 * <p>Base path: {@code /api/questions}</p>
 *
 * <p>Authorization rules:</p>
 * <ul>
 *   <li>GET endpoints are publicly accessible</li>
 *   <li>POST and DELETE endpoints require {@code ROLE_ADMIN}</li>
 * </ul>
 */

@RestController
@RequestMapping("/api/questions")
@Tag( name = "Question", description = "Statement management API")
/**
 * Creates a new question controller.
 *
 * @param questionService service responsible for business logic
 * @param questionRepository repository for direct question lookup
 */
public class QuestionController {

    // diese Variable wird erzeugt und über Konstruktor initialisert um
    // nachher auf alle Methoden der Klasse QuestionService zugreifen zu können
    private final QuestionService questionService;
    private final QuestionRepository questionRepository;

    @Autowired
    public QuestionController(QuestionService questionService, QuestionRepository questionRepository) {
        this.questionService = questionService;
        this.questionRepository = questionRepository;
    }

    /**
     * Returns all available questions.
     *
     * <p>HTTP: {@code GET /api/questions}</p>
     *
     * @return list of {@link Question} entities
     */

    @GetMapping
    public ResponseEntity<List<Question>> getQuestions() {
        List<Question> questions = questionService.getAllQuestions();
        return ResponseEntity           // ResponseEntity wird erzeugt
                .status(HttpStatus.OK)  // Statuscode wird auf OK gesetzt
                .body(questions);       // body wird definiert
    }

    /**
     * Returns a specific question by its ID.
     *
     * <p>HTTP: {@code GET /api/questions/{questionId}}</p>
     *
     * @param questionId ID of the question
     * @return {@link Question} if found,
     *         {@code 404 Not Found} if no question exists with the given ID
     */

    @GetMapping("/{questionId}")
    public ResponseEntity<Question> getOneQuestionById(@PathVariable Long questionId) {
        Optional<Question> result = questionRepository.findById(questionId);
        if(result.isPresent()) {
            return ResponseEntity.status( HttpStatus.OK )
                    .body( result.get() );
        } else {
            return new ResponseEntity<>( HttpStatus.NOT_FOUND );
        }
    }
    /**
     * Creates a new question.
     *
     * <p>HTTP: {@code POST /api/questions}</p>
     *
     * <p>Access: {@code ROLE_ADMIN} only.</p>
     *
     * @param question textual content of the new question
     * @return created {@link Question} with status {@code 201 Created}
     *         or {@code 400 Bad Request} if validation fails
     */

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createQuestion(@RequestBody String question)
    {
        try {
            Question createdQuestion = questionService.createQuestion(question);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdQuestion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Deletes a question by its ID.
     *
     * <p>HTTP: {@code DELETE /api/questions/{questionId}}</p>
     *
     * <p>Access: {@code ROLE_ADMIN} only.</p>
     *
     * @param questionId ID of the question to delete
     * @return {@code 204 No Content} if deletion was successful
     */

    @DeleteMapping("/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteQuestionById(@PathVariable Long questionId)
    {
        questionService.deleteQuestionById(questionId);
        return ResponseEntity.noContent().build();
    }

}