package htw.wahlomat.wahlomat.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import htw.wahlomat.wahlomat.dto.CandidacyListRequest;
import htw.wahlomat.wahlomat.model.CandidacyList;
import htw.wahlomat.wahlomat.model.Role;
import htw.wahlomat.wahlomat.model.User;
import htw.wahlomat.wahlomat.model.profilePage.Candidacy;
import htw.wahlomat.wahlomat.model.profilePage.CandidateProfile;
import htw.wahlomat.wahlomat.repository.CandidacyListRepository;
import htw.wahlomat.wahlomat.repository.profilePage.CandidacyRepository;
import htw.wahlomat.wahlomat.repository.profilePage.CandidateProfileRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CandidacyListControllerTest
{
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper; // um aus useren Objekten jsons zu machen und zurück in Objekte wandeln
    private final CandidacyListRepository candidacyListRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final CandidacyRepository candidacyRepository;

    // Variablen für Test-Objekte
    static User user1;
    static CandidateProfile candidate1;
    static CandidacyList list1;
    static Candidacy candidacy1;

    // Konstruktor
    @Autowired
    public CandidacyListControllerTest(MockMvc mockMvc,
                                        ObjectMapper objectMapper,
                                        CandidacyListRepository candidacyListRepository,
                                       CandidateProfileRepository candidateProfileRepository,
                                       CandidacyRepository candidacyRepository)
    {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.candidacyListRepository = candidacyListRepository;
        this.candidateProfileRepository = candidateProfileRepository;
        this.candidacyRepository = candidacyRepository;
    }

    @BeforeEach
    public void setUp()
    {
        // Testdaten bereinigen (spezifische IDs nur)
        cleanupTestData();

        // Initialisierung von Test-Objekten mit eindeutiger Test-ID (999L)
        // um Konflikte mit echten Daten zu vermeiden
        user1 = new User("testuser1", "password", Role.CANDIDATE);
        candidate1 = new CandidateProfile(user1, "Max", "Mustermann", 1L, "Ich bin ein Testkandidat.");
        list1 = new CandidacyList(1, "Testliste", "FSR1");

        // Speichern in korrekter Reihenfolge
        candidate1 = candidateProfileRepository.saveAndFlush(candidate1);
        list1 = candidacyListRepository.saveAndFlush(list1);

        // Candidacy mit korrekten Foreign Keys erstellen
        candidacy1 = new Candidacy(user1, "FSR1", list1);
        candidacy1 = candidacyRepository.saveAndFlush(candidacy1);
    }

    private void cleanupTestData() {
        try {
            // 1. Erst Candidacies mit Test-User-ID löschen
            List<Candidacy> candidaciesToDelete = candidacyRepository.findByUser(user1);
            if (!candidaciesToDelete.isEmpty()) {
                candidacyRepository.deleteAll(candidaciesToDelete);
                candidacyRepository.flush();
            }

            // 2. Dann CandidateProfile mit Test-User-ID löschen
            Optional<CandidateProfile> existingProfile = candidateProfileRepository.findByUser(user1);
            if (existingProfile.isPresent()) {
                // UserId wird aus user1 über das CandidateProfile-Objekt geholt
                candidateProfileRepository.deleteById(existingProfile.get().getUser().getUserId());
                candidateProfileRepository.flush();
            }

            // 3. CandidacyLists mit Test-Namen löschen
            candidacyListRepository.findAll().stream()
                .filter(list -> "Testliste".equals(list.getListName()) && "FSR1".equals(list.getCommitteeId()))
                .forEach(list -> {
                    candidacyListRepository.deleteById(list.getListId());
                    candidacyListRepository.flush();
                });
        } catch (Exception e) {
            // Ignoriere Fehler beim Cleanup - kann passieren wenn Daten bereits gelöscht sind
            System.out.println("Cleanup warning: " + e.getMessage());
        }
    }

    @AfterEach
    public void delete()
    {
        // Bereinige nur die spezifischen Test-Daten
        cleanupTestData();
    }

    @Test
    void create_CandidacyList() throws Exception
    {
        // CandidacyListRequest object für HTTP Request erstellen
        CandidacyListRequest candidacyListRequest = new CandidacyListRequest(2, "Liste123456789", "FSR1");

        mockMvc.perform(post("/api/lists")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(candidacyListRequest))) // User-Objekt als JSON im Request-Body
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.listId").isNotEmpty())
                .andExpect(jsonPath("$.listName", is("Liste123456789")));

        // Bereinigen: Erstellte Liste löschen
        Optional<CandidacyList> candidacyList = candidacyListRepository.findByListName("Liste123456789");
        if (candidacyList.isPresent()) {
            candidacyListRepository.deleteById(candidacyList.get().getListId());
        }
    }

    @Test
    void getAllCandidacyLists_NotEmpty() throws Exception
    {
        mockMvc.perform(get("/api/lists")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$[-1].listName", is("Testliste")));
    }

    @Test
    void getAllCandidacyLists_ByCommitteeId_existingCommittees() throws Exception
    {
        mockMvc.perform(get("/api/lists/" + list1.getCommitteeId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$[0].committeeId", is("FSR1")));
    }

/*
    @Test
    void deleteExistingCandidacyList() throws Exception
    {
        Long listIdToDelete = list1.getListId();

        // Lösch-Request ausführen
        mockMvc.perform(delete("/api/lists/" + listIdToDelete))
                .andExpect(status().isNoContent());

        // Überprüfen, ob zugehörige Candidacy-Einträge gelöscht wurden
        List<Candidacy> associatedCandidacies = candidacyRepository.findByListId(listIdToDelete);
        assertThat(associatedCandidacies).isEmpty();

        // Überprüfen, ob die Liste gelöscht wurde
        Optional<CandidacyList> deletedList = candidacyListRepository.findById(listIdToDelete);
        assertThat(deletedList).isEmpty();
    }
*/

}
