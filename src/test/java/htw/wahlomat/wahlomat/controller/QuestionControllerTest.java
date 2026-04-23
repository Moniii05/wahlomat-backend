package htw.wahlomat.wahlomat.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import htw.wahlomat.wahlomat.model.Question;
import htw.wahlomat.wahlomat.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc

public class QuestionControllerTest {
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final QuestionRepository questionRepository;


    //statische Beispiel Question-Objekte - evtl sollte man hier DTOs nutzen anstatt Gesamtobjekte
    static Question question1, question2;


    //Konstruktor
    @Autowired
    public QuestionControllerTest(MockMvc mockMvc,
                                  ObjectMapper objectMapper,
                                  QuestionRepository questionRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.questionRepository = questionRepository;
    }


    // entfernt alle Testeinträge vor jedem neuen Test, um ungewollte Nebeneffekte im Test zu vermeiden
    //stellt Dummydaten zur Verfügung, Dekorator zeigt an, dass Methode einmalig vor allen anderen ausgeführt wird
    @BeforeEach
    void setUp() {
        cleanUp();

        question1 = this.questionRepository.save(new Question("Es soll einen Zugang zur Uni vom Wasser aus geben"));

        question2 = this.questionRepository.save(new Question("Jeder Seminarraum braucht eine Zimmerpflanze"));
    }

    void cleanUp() {
        try {
            // Suche Fragen anhand des Textes und lösche sie über die ID
            Question q1 = questionRepository.findByQuestion("Es soll einen Zugang zur Uni vom Wasser aus geben");
            if (q1 != null) {
                questionRepository.deleteById(q1.getQuestionId());
            }

            Question q2 = questionRepository.findByQuestion("Jeder Seminarraum braucht eine Zimmerpflanze");
            if (q2 != null) {
                questionRepository.deleteById(q2.getQuestionId());
            }

            questionRepository.flush();
        } catch (Exception e) {
            System.out.println("Fehler beim Cleanup: " + e.getMessage());
        }
    }


    // einzelner Test, Name der Methode beschreibt, was getestet werden soll.
    // hier: Test des Endpunkts zum Abruf aller Fragen
    @Test
    void getAllQuestions() throws Exception {

        mockMvc.perform(get("/api/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].questionId", hasItems(
                        question1.getQuestionId().intValue(),
                        question2.getQuestionId().intValue()
                )));
    }

    // Wenn Elemente by ID getestet werden oft sinnvoller, Name oder text zu verifizieren
    @Test
    void getQuestionByIdTest() throws Exception {

        mockMvc.perform(get("/api/questions/" + question1.getQuestionId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionId", is(question1.getQuestionId().intValue())))
                .andExpect(jsonPath("$.question", containsString("Es soll einen Zugang zur Uni vom Wasser aus geben")));

    }

    // Rückgabe 404 wenn ID nicht existiert
    @Test
    void getQuestionById_NotFound() throws Exception {

        mockMvc.perform(get("/api/questions/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createQuestion_Success() throws Exception
    {
        String newQuestionText = "Die Mensa sollte 24 Stunden geöffnet sein.";

        mockMvc.perform(
                        post("/api/questions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(newQuestionText)) // Question-Text als JSON im Request-Body
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.questionId").isNotEmpty())
                .andExpect(jsonPath("$.question", is(newQuestionText)));

        //Bereinigen: Erstellte Frage löschen
        Question createdQuestion = questionRepository.findByQuestion(newQuestionText);
        if (createdQuestion != null)
        {
            questionRepository.deleteById(createdQuestion.getQuestionId());

        }
    }

    @Test
    void createExistingQuestion_ShouldReturnError() throws Exception
    {
        String existingQuestionText = question1.getQuestion();

        mockMvc.perform(
                        post("/api/questions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(existingQuestionText)) // Question-Text als JSON im Request-Body
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", containsString("existiert bereits")));
    }

    @Test
    void deleteQuestionById_Success() throws Exception
    {
        mockMvc.perform(delete("/api/questions/" + question2.getQuestionId()))
                .andExpect(status().isNoContent());

        // Überprüfen, ob die Frage tatsächlich gelöscht wurde
        assertTrue(questionRepository.findById(question2.getQuestionId()).isEmpty());
    }


}



/* übliche Tests von Get Endpunkten:
immer Statuscode & Header & Body (z.B. Inhalt, Anzahl)
zusätzlich möglich:
korrekter Umgang mit leerem Datenset
Zugriffskontrolle- falls vorhanden Autorisierungsregeln testen
Edge Cases- Wenn Paging oder Suchkriterien vom Endpunkt akzeptiert werden, dann sollte das getestet werden
 */


