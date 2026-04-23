package htw.wahlomat.wahlomat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import htw.wahlomat.wahlomat.dto.CandidateProfileRequest;
import htw.wahlomat.wahlomat.dto.CandidateProfileResponse;
import htw.wahlomat.wahlomat.model.User;
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

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CandidateController.class)
@AutoConfigureMockMvc(addFilters = false)
/**
 Test für CandidateController
 UserRepository und CandidateService werden gemockt
 */
class CandidateProfileControllerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;

  @MockitoBean CandidateService candidateService;
  @MockitoBean UserRepository userRepository;

  @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Test
  void getProfile_userExists_200() throws Exception {
    long userId = 1L;
    User user = Mockito.mock(User.class);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(candidateService.getProfile(user))
        .thenReturn(new CandidateProfileResponse("Robert","Baum",4L,"Hi"));

    mvc.perform(get("/api/candidate-profiles/{userId}", userId))
       .andExpect(status().isOk())
       .andExpect(content().contentType(MediaType.APPLICATION_JSON))
       .andExpect(jsonPath("$.firstname").value("Robert"))
       .andExpect(jsonPath("$.lastname").value("Baum"))
       .andExpect(jsonPath("$.facultyId").value(4))
       .andExpect(jsonPath("$.aboutMe").value("Hi"));
  }

  @Test
  void getProfile_userMissing_404() throws Exception {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    mvc.perform(get("/api/candidate-profiles/{userId}", 999L))
       .andExpect(status().isNotFound());
  }

  @Test
  void saveProfile_userExists_200() throws Exception {
    long userId = 1L;
    User user = Mockito.mock(User.class);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    var body = Map.of(
        "firstname","Robert",
        "lastname","Baum",
        "facultyId",4,
        "aboutMe","Hi 👋"
    );

    mvc.perform(put("/api/candidate-profiles/{userId}", userId)
          .contentType(MediaType.APPLICATION_JSON)
          .content(mapper.writeValueAsString(body)))
       .andExpect(status().isOk());

    // Wurde Service mit User + DTO aufgerufen?
    verify(candidateService).saveProfile(eq(user), any(CandidateProfileRequest.class));
  }

  @Test
  void saveProfile_userMissing_404() throws Exception {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    var body = Map.of(
        "firstname","X",
        "lastname","Y",
        "facultyId",1,
        "aboutMe","z"
    );

    mvc.perform(put("/api/candidate-profiles/{userId}", 999L)
          .contentType(MediaType.APPLICATION_JSON)
          .content(mapper.writeValueAsString(body)))
       .andExpect(status().isNotFound());
  }
}