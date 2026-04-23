package htw.wahlomat.wahlomat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import htw.wahlomat.wahlomat.model.Role;
import htw.wahlomat.wahlomat.model.User;
import htw.wahlomat.wahlomat.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


// Tests für UserController -> GET api/user & GET api/user/{userId}

@SpringBootTest
@AutoConfigureMockMvc //Anfragen und Responses wollen wir mocken mit mockmvc. Nimmt Requests entgegen uns sendet responses ohne eigen Http server
public class UserControllerTest
{
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper; // um aus useren Objekten jsons zu machen und zurück in Objekte wandeln
    private final UserRepository userRepository;

    static User userA, userC, userUniqueEmail, userC_wrongPassword;



    // injizieren der oben genannten Variablen über folgenden Konstruktor
    @Autowired
    public UserControllerTest(MockMvc mockMvc,
                              ObjectMapper objectMapper,
                              UserRepository userRepository)
    {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
    }
/*
    // Methode wird als allererstes und genau einmal ausgeführt
    @BeforeAll
    public static void init()
    {
        userA = new User("pass1234", "userA@test.de", Role.ADMIN);
        // Admin
        userC = new User("pass1234", "userC@test.de", Role.CANDIDATE);
        // Candidate
        userUniqueEmail = new User( "pass1234", "userA@test.de", Role.ADMIN);
        //User Admin doppeltes Vorkommen Mail testen
        userC_wrongPassword = new User( "pass1235", "userC@test.de", Role.CANDIDATE);
        //User Candidate falsches PW Test
    }

 */

    /*
    @Test
    // Test simuliert Anfrage: User wird angefragt, aber Liste ist leer
    public void getUser_EmptyList() throws Exception
    {
        // perform stellt Anfrage /simuliert Anfrage an den Http server

        mockMvc.perform(get("/api/users")) // wir mocken eine Anfrage an Get Enpunkt, kann eine Exception werfen
                .andExpect(status().isOk()) // was wir erwarten ,import oben beachten
                .andExpect(jsonPath("$", hasSize(0)));// die Body Response können wir hier direkt ansprechen mit Hilfe $. Wir gehen davon aus, dass das Objekt leer ist. Mit Punktnotation können wir auch auf Eigenschaften des Objekts zurgreifen
    }

     */


    @Test
    public void getUser_UserListNotEmpty() throws Exception {
        // neuen User erstellen
        User testUser = new User("","test@test.de", Role.CANDIDATE) ;
        //testUser in DB hinzufügen
        this.userRepository.save(testUser) ;

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0)))) // größer als leer // $ ist unsere Collection, unser Response Body, ist ein Array aus lauter JSON Objekten
                .andExpect(jsonPath("$[*].email", hasItem(testUser.getEmail()))); // bekommen ein Array an usern zurück und fragen, ob unser testUser dabei ist mit der WILDCARD ist Platz im Array beliebig

        // testUser löschen um Störungen zu vermeiden
        this.userRepository.delete(testUser);
    }

    @Test
    //Test simuliert Anfrage: User by ID
    public void getUserById_UserExists() throws Exception
    {
        // bekommen vom dem Endpunkt den User zurück mit ID, daher müssen wir uns User speichern, um an die ID zukommen, denn wir kennen sie ja nicht
        User savedUser= this.userRepository.save(new User( "", "test@mail.de",  Role.CANDIDATE));

        mockMvc.perform(get("/api/users/" + savedUser.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", containsString("test@mail.de")))
                .andExpect(jsonPath("$.id", is(savedUser.getUserId().intValue()))); // ohne Umwandlung Typkonflikt, da Id ein Long ist

// löschen, um Störungen zu vermeiden
        this.userRepository.delete(savedUser);

    }




}
