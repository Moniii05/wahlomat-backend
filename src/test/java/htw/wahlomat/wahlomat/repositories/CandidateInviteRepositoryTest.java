package htw.wahlomat.wahlomat.repositories;

import htw.wahlomat.wahlomat.model.admin.CandidateInvite;
import htw.wahlomat.wahlomat.repository.CandidateInviteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

// diese Anotation macht die Tests grunsätzlich transactional
@DataJpaTest
@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=create-drop"})
public class CandidateInviteRepositoryTest {

    @Autowired
    CandidateInviteRepository candidateInviteRepository;

    // entityManager übernimmt Datenbankoperationen für uns im Setup, so dass im Test wirklich RepoMethodenaufrufe getestet werden
    @Autowired
    TestEntityManager entityManager;

    // Testen der Methode findByEmail, diese wird im AuthService register() genutzt
    @Test
    void givenCandidateInviteCreated_whenFindByEmail_thenSuccess(){

        CandidateInvite newCandidateInvite = new CandidateInvite("lenatest@htw-berlin.de");
        entityManager.persist(newCandidateInvite);
        Optional<CandidateInvite> retrievedCandidateInvite =
                candidateInviteRepository.findByEmail(newCandidateInvite.getEmail());
        assertThat(retrievedCandidateInvite).contains(newCandidateInvite);
    }


}
