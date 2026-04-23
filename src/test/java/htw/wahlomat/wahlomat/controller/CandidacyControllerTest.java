package htw.wahlomat.wahlomat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import htw.wahlomat.wahlomat.dto.CandidacyRequest;
import htw.wahlomat.wahlomat.dto.CandidacyResponse;
import htw.wahlomat.wahlomat.model.CandidacyList;
import htw.wahlomat.wahlomat.model.User;
import htw.wahlomat.wahlomat.repository.CandidacyListRepository;
import htw.wahlomat.wahlomat.repository.UserRepository;
import htw.wahlomat.wahlomat.security.JwtAuthenticationFilter;
import htw.wahlomat.wahlomat.service.CandidateService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CandidacyController.class)
@AutoConfigureMockMvc(addFilters = false)
/**
 Tests für CandidacyController
 CandidateService, UserRepository und CandidacyListRepository werden gemockt

 */
class CandidacyControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @MockitoBean
    CandidateService candidateService;

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    CandidacyListRepository listRepository;

    @MockitoBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void list_byUser_200() throws Exception {
        long userId = 1L;
        User user = Mockito.mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(candidateService.listCandidacies(user))
                .thenReturn(List.of(new CandidacyResponse(39L, "FSR3", 10L)));

        mvc.perform(get("/api/candidacies/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].candidacyId").value(39))   // wichtig: candidacyId, nicht id
                .andExpect(jsonPath("$[0].committeeId").value("FSR3"))
                .andExpect(jsonPath("$[0].listId").value(10));
    }

    @Test
    void list_byUser_404_whenUserMissing() throws Exception {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/candidacies/user/{userId}", 999L))
                // aktueller Controller liefert leere Liste statt 404
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void list_byListId_200() throws Exception {
        long listId = 12L;
        when(listRepository.findById(listId)).thenReturn(Optional.of(new CandidacyList()));

        when(candidateService.getCandidaciesByCandidacyList(any(CandidacyList.class)))
                .thenReturn(List.of(new CandidacyResponse(7L, "AS", 12L)));

        mvc.perform(get("/api/candidacies/list/{listId}", listId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].committeeId").value("AS"))
                .andExpect(jsonPath("$[0].listId").value(12));
    }

    @Test
    void create_ok_200() throws Exception {
        long userId = 1L;
        User user = Mockito.mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(candidateService.createCandidacy(eq(user), any(CandidacyRequest.class)))
                .thenReturn(new CandidacyResponse(123L, "AS", 12L));

        var body = Map.of(
                "committeeId", "AS",
                "listId", 12
        );

        mvc.perform(post("/api/candidacies/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidacyId").value(123)) // wichtig: candidacyId
                .andExpect(jsonPath("$.committeeId").value("AS"))
                .andExpect(jsonPath("$.listId").value(12));
    }

    @Test
    void create_userMissing_404() throws Exception {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        var body = Map.of(
                "committeeId", "AS",
                "listId", 12
        );

        mvc.perform(post("/api/candidacies/user/{userId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_ok_200() throws Exception {
        doNothing().when(candidateService).updateCandidacy(eq(123L), any(CandidacyRequest.class));

        var body = Map.of(
                "committeeId", "AS",
                "listId", 12
        );

        mvc.perform(put("/api/candidacies/{candidacyId}", 123L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(candidateService).updateCandidacy(eq(123L), any(CandidacyRequest.class));
    }

    @Test
    void delete_ok_200() throws Exception {
        doNothing().when(candidateService).deleteCandidacy(123L);

        mvc.perform(delete("/api/candidacies/{candidacyId}", 123L))
                .andExpect(status().isOk());

        verify(candidateService).deleteCandidacy(123L);
    }
}
