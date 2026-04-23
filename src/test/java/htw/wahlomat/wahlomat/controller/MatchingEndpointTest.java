package htw.wahlomat.wahlomat.controller;

//eigentlich ein Endpunkt aus CandidateAnswerController.
//Wenn die Testklasse wieder funktioniert, kann dieser integriert werden

import com.fasterxml.jackson.databind.ObjectMapper;
import htw.wahlomat.wahlomat.dto.CandidateMatchingResult;
import htw.wahlomat.wahlomat.dto.MatchingRequest;
import htw.wahlomat.wahlomat.model.AnswerOption;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MatchingEndpointTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Autowired
    public MatchingEndpointTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    //Contract Test (API Vertrag)
    @Test
    public void testMatchingEndpoint() throws Exception {

        List<MatchingRequest> voterAnswers = List.of(
                new MatchingRequest(1L, AnswerOption.STIMME_ZU, false),
                new MatchingRequest(2L, AnswerOption.NEUTRAL, false),
                new MatchingRequest(3L, AnswerOption.STIMME_NICHT_ZU, true)
        );

        String requestJson = objectMapper.writeValueAsString(voterAnswers);

        String committeeId = "FSR1";

        mockMvc.perform(post("/api/candidate-profiles/matching/{committeeId}", committeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    //testet: gültiges JSON? Passt es zum DTO?
                    String content = result.getResponse().getContentAsString();
                    objectMapper.readValue(content, CandidateMatchingResult[].class);
                });

    }
}
