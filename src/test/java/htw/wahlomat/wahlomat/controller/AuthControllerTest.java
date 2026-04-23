// Testen der User-Registrierung und User-Login im AuthController

package htw.wahlomat.wahlomat.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

// Imports weiterer Strukturen aus dem Projekt
import static htw.wahlomat.wahlomat.model.Role.CANDIDATE;
import htw.wahlomat.wahlomat.model.User;
import htw.wahlomat.wahlomat.model.profilePage.Candidacy;
import htw.wahlomat.wahlomat.model.profilePage.CandidateProfile;
import htw.wahlomat.wahlomat.model.admin.CandidateInvite;
import htw.wahlomat.wahlomat.model.admin.InviteStatus;

import htw.wahlomat.wahlomat.dto.LoginRequest;
import htw.wahlomat.wahlomat.dto.CandidacyRequest;
import htw.wahlomat.wahlomat.dto.RegisterRequest;

import htw.wahlomat.wahlomat.repository.UserRepository;
import htw.wahlomat.wahlomat.repository.profilePage.CandidacyRepository;
import htw.wahlomat.wahlomat.repository.CandidacyListRepository;
import htw.wahlomat.wahlomat.repository.profilePage.CandidateProfileRepository;
import htw.wahlomat.wahlomat.repository.CandidateInviteRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

// Imports für Tests
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthControllerTest
{
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CandidacyRepository candidacyRepository;
    private final CandidacyListRepository candidacyListRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final CandidateInviteRepository candidateInviteRepository;
    private final Logger logger = LoggerFactory.getLogger(AuthControllerTest.class);

    // Variablen für Test-Objekte
    static User user1;

    @Autowired
    public AuthControllerTest(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            CandidacyRepository candidacyRepository,
            CandidacyListRepository candidacyListRepository,
            CandidateProfileRepository candidateProfileRepository,
            CandidateInviteRepository candidateInviteRepository
    ) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.candidacyRepository = candidacyRepository;
        this.candidacyListRepository = candidacyListRepository;
        this.candidateProfileRepository = candidateProfileRepository;
        this.candidateInviteRepository = candidateInviteRepository;
    }

    @BeforeEach
    public void setUp()
    {

        //Intitalisierung von Test-Objekten


        //Invite für Kandidaten nötig, um register() Test laufen zu lassen
        CandidateInvite invite = new CandidateInvite("test999@student.htw-berlin.de");
        invite.setStatus(InviteStatus.INVITED);
        invite.setInvitedAt(Instant.now());
        candidateInviteRepository.saveAndFlush(invite);

        // User Erstellung für den Login Test, Passwort muss bereits gehasht werden, da direkter Eintrag in die DB
        String rawpassword = "123456";
        user1 = new User( passwordEncoder.encode(rawpassword), "test888@student.htw-berlin.de", CANDIDATE);
        userRepository.saveAndFlush(user1);

        logger.info("Setup complete, starting test...");
    }



@Test
void registerUser_WithValidData_ShouldReturnSuccessAndSavedUserWithRoleCandidate() throws Exception{

    // RegisterRequest und CandidacyRequest Objekt für Simualtion des HTTP Request erstellen

    CandidacyRequest candidacy1= new CandidacyRequest("FSR4", 1L);
    CandidacyRequest candidacy2 = new CandidacyRequest("STUPA", 2L);
    List<CandidacyRequest> candidacies=
            Arrays.asList(candidacy1, candidacy2);

    RegisterRequest registerRequest = new RegisterRequest(
            "test999@student.htw-berlin.de",
            "123456",
            "Testa",
            "Testing",
            1L,
            "Testbio user1",
            candidacies);

// Hier passiert jetzt die eigentliche Überprüfung. Die Assertions überprüfen die Werte in der AuthResponse
// bezüglich Token: Kann überprüft werden mit .andExpect(jsonPath("$.token").isNotEmpty()); da genaue Tokenwert Validierung zu kompliziert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is ("test999@student.htw-berlin.de")))
                .andExpect(jsonPath("$.role", is ("CANDIDATE")));

// Da das Passwort nicht in der AuthResponse ist, wird die Assertion hier über den Aufruf des Users aus der Datenbank realisiert
        Optional<User> savedUser = userRepository.findByEmail("test999@student.htw-berlin.de");
        if(savedUser.isPresent()){
            assertTrue(passwordEncoder.matches("123456", savedUser.get().getPassword()));
        }

        // Aufruf der Cleanup Methode über Method Reference --> Die Mehtode wird auf dieser Klassenreferenz von User aufgerufen
        savedUser.ifPresent(this::cleanupTestData);


}


   //dieser Test läuft auch ohne LoginRequest-DTO? --> Weil es zu JSON umgewandelt wird und die restlichen Felder ignoriert werden
    @Test
    void candidateLogin_WithValidCredentials_ShouldReturnSuccessAndToken() throws Exception {
        Optional<User> optionalUser = userRepository.findByEmail(user1.getEmail());
        if (optionalUser.isEmpty()) {
            // Benutzer in die DB einfügen, falls nicht vorhanden
            userRepository.save(user1);
        }

        // Erstellung des LoginRequest
        LoginRequest loginRequest = new LoginRequest(user1.getEmail(), "123456");

        // Login-Request simulieren
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))) // User-Objekt als JSON im Request-Body
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email", is("test888@student.htw-berlin.de")))
                .andExpect(jsonPath("$.message", is("Login erfolgreich")));

    }

    @Test
    void candidateLogin_WrongPassword_ShouldReturnUnauthorized() throws Exception
    {
        if(userRepository.findByEmail(user1.getEmail()).isEmpty())
        {
            // User in DB anlegen
            userRepository.save(user1);
        }

        // LoginRequest mit falschem Passwort simulieren
        LoginRequest loginRequest = new LoginRequest("test888@student.htw-berlin.de", "wrongpass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    // Methode zum Bereinigen der Testdaten --> muss in umgekehrter Reihenfolge der Abhängigkeiten vorgehen
    private void cleanupTestData(User user) {

        logger.info("Cleaning up test data...");



        try {
            List<Candidacy> candidaciesToDelete = candidacyRepository.findByUser(user);
            if (!candidaciesToDelete.isEmpty()) {
                candidacyRepository.deleteAll(candidaciesToDelete);
                candidacyRepository.flush();
            }
            Optional<CandidateProfile> existingProfile = candidateProfileRepository.findByUser(user);
            if (existingProfile.isPresent()) {
                // geschachtelter Aufruf: UserId wird aus user1 über das CandidateProfile-Objekt geholt
                candidateProfileRepository.deleteById(existingProfile.get().getUser().getUserId());
                candidateProfileRepository.flush();
            }
            candidacyListRepository.findAll().stream()
                    .filter(list -> "Testliste 1".equals (list.getListName()) && "FSR4".equals(list.getCommitteeId()))
                    .forEach(list -> {
                        candidacyListRepository.deleteById(list.getListId());
                        candidacyListRepository.flush();
                    });

            Optional<CandidateInvite> existingInvite = candidateInviteRepository.findByEmail(user.getEmail());
            if(existingInvite.isPresent()){
                candidateInviteRepository.deleteById(existingInvite.get().getId());
                candidateInviteRepository.flush();
            }

            userRepository.deleteById(user.getUserId());
            userRepository.flush();

        } catch(Exception e){
            System.out.println("CleanUp warning: " + e.getMessage());
        }

    }


}
