package htw.wahlomat.wahlomat.repositories;

import htw.wahlomat.wahlomat.model.Role;
import htw.wahlomat.wahlomat.model.User;
import htw.wahlomat.wahlomat.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource (properties = {"spring.jpa.hibernate.ddl-auto=create-drop"})


public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;


    // Testen der Methode existsByEmail, diese wird im AuthService register() genutzt
    @Test
    void givenUserCreated_whenExistsByEmail_thenTrue(){

        //arrange: Testdaten präparieren

        User testUSer = new User("lenatest@htw-berlin.de", Role.CANDIDATE);
        entityManager.persistAndFlush(testUSer);

        //act: Aufruf der Methode, die getestet werden soll
        boolean exists =
                userRepository.existsByEmail("lenatest@htw-berlin.de");

       //assert: überprüft den erwarteten Boolean-Wert
        assertThat(exists).isTrue();
    }

    // Testen der erfolgreichen Anlage neuer User direkt übers Repository
    @Test
    void givenNewUser_whenSave_thenSuccess(){
        //arrange: Testdaten präparieren
        User newUser = new User ("123456","lenatest@htw-berlin.de",Role.CANDIDATE);

        // act: Aufruf der Methode die getestet werden soll, Speichern des Ergebnis in eigener Hilfsvariable
        User insertedUser = userRepository.save(newUser);

        // assert: Abgleich über UserId- mit entityManager wird insertedUser geholt, und die Id mit der von newUser Instanz verglichen
        assertThat(entityManager.find(User.class,insertedUser.getUserId())).isEqualTo(newUser);
    }


}
