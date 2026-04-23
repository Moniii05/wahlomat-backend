package htw.wahlomat.wahlomat.config;

import htw.wahlomat.wahlomat.model.Question;
import htw.wahlomat.wahlomat.model.User;
import htw.wahlomat.wahlomat.model.profilePage.CandidateProfile;
import htw.wahlomat.wahlomat.repository.QuestionRepository;
import htw.wahlomat.wahlomat.repository.UserRepository;
import htw.wahlomat.wahlomat.repository.profilePage.CandidateProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class LoadTestData {
    @Bean
    @Order(3) // Nach loadTestCandidates (Order 2)
    public CommandLineRunner loadQuestions(
            QuestionRepository questionRepository,
            @Value("${spring.profiles.active:dev}") String activeProfile) {

        return args -> {
            if (!activeProfile.equals("dev")) {
                return;
            }

            System.out.println("\n═══════════════════════════════════════════");
            System.out.println("  TEST-QUESTIONS");
            System.out.println("═══════════════════════════════════════════");

            if (questionRepository.count() == 0) {
                Question q1 = new Question();
                q1.setQuestion("Die HTW sollte mehr Geld in die Begrünung des Campus investieren.");
                questionRepository.save(q1);  // ← Automatische Transaktion!

                Question q2 = new Question();
                q2.setQuestion("Es sollten mehr Veranstaltungen für Studierende angeboten werden.");
                questionRepository.save(q2);  // ← Automatische Transaktion!

                Question q3 = new Question();
                q3.setQuestion("Mir ist wichtig, dass es in der Mensa mehr vegane/vegetarische als fleischlastige Gerichte gibt.");
                questionRepository.save(q3);  // ← Automatische Transaktion!

                System.out.println("3 Test-Questions erstellt");
            } else {
                System.out.println("Questions bereits vorhanden (" + questionRepository.count() + " Fragen)");
            }

            System.out.println("═══════════════════════════════════════════\n");
        };
    }

    @Bean
    @Order(4)
    public CommandLineRunner loadCandidateProfiles(
            UserRepository userRepository,
            CandidateProfileRepository candidateProfileRepository,
            PlatformTransactionManager transactionManager,
            @Value("${spring.profiles.active:dev}") String activeProfile) {

        return args -> {
            if (!activeProfile.equals("dev")) {
                return;
            }

            System.out.println("\n═══════════════════════════════════════════");
            System.out.println("  TEST-CANDIDATE-PROFILES");
            System.out.println("═══════════════════════════════════════════");

            // Alles in einer Transaktion ausführen -> sonst error: "detached entity"
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionTemplate.execute(status -> {
                // Anna's Profile
                User anna = userRepository.findByEmail("anna@stud.htw-berlin.de").orElse(null);
                if (anna != null && !candidateProfileRepository.existsById(anna.getUserId())) {
                    CandidateProfile annaProfile = new CandidateProfile();
                    annaProfile.setUser(anna);
                    annaProfile.setFirstname("Anna");
                    annaProfile.setLastname("Schmidt");
                    annaProfile.setFacultyId(1L);
                    annaProfile.setAboutMe("Ich setze mich für Nachhaltigkeit und Umweltschutz auf dem Campus ein.");
                    candidateProfileRepository.save(annaProfile);
                    System.out.println("Profil für Anna erstellt");
                } else if (anna != null) {
                    System.out.println("Profil für Anna existiert bereits");
                }

                // Bob's Profile
                User bob = userRepository.findByEmail("bob@stud.htw-berlin.de").orElse(null);
                if (bob != null && !candidateProfileRepository.existsById(bob.getUserId())) {
                    CandidateProfile bobProfile = new CandidateProfile();
                    bobProfile.setUser(bob);
                    bobProfile.setFirstname("Bob");
                    bobProfile.setLastname("Müller");
                    bobProfile.setFacultyId(2L);
                    bobProfile.setAboutMe("Ich möchte mehr Events und Networking-Möglichkeiten für Studierende schaffen.");
                    candidateProfileRepository.save(bobProfile);
                    System.out.println("Profil für Bob erstellt");
                } else if (bob != null) {
                    System.out.println("Profil für Bob existiert bereits");
                }

                return null;
            });

            System.out.println("═══════════════════════════════════════════\n");
        };
    }
}

