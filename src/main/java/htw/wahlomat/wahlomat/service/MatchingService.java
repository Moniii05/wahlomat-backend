package htw.wahlomat.wahlomat.service;

import htw.wahlomat.wahlomat.dto.CandidateMatchingResult;
import htw.wahlomat.wahlomat.dto.MatchingRequest;
import htw.wahlomat.wahlomat.model.*;
import htw.wahlomat.wahlomat.model.profilePage.Candidacy;
import htw.wahlomat.wahlomat.model.profilePage.CandidateProfile;
import htw.wahlomat.wahlomat.repository.CandidateAnswerRepository;
import htw.wahlomat.wahlomat.repository.QuestionRepository;
import htw.wahlomat.wahlomat.repository.profilePage.CandidacyRepository;
import htw.wahlomat.wahlomat.repository.profilePage.CandidateProfileRepository;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service that calculates the matching result between a voter and all candidates
 * of a given committee.
 *
 * <p>The matching is based on the (weighted) euclidean distance between the voter's
 * selected options and the candidate's selected options. Skipped voter answers
 * ({@link AnswerOption#FRAGE_UEBERSPRINGEN}) are ignored.
 *
 * <p>Additionally, only candidates with a complete answer set (all questions answered,
 * no null answers) are included in the committee result.
 */
@Service
public class MatchingService {

    private final CandidateAnswerRepository candidateAnswerRepository;
    private final CandidacyRepository candidacyRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final QuestionRepository questionRepository;

    /**
     * Creates a new {@link MatchingService}.
     *
     * @param candidateAnswerRepository repository to load candidate answers
     * @param candidacyRepository repository to load candidacies per committee
     * @param candidateProfileRepository repository to load candidate profiles
     * @param questionRepository repository to load all questions (for completeness check)
     */
    public MatchingService(CandidateAnswerRepository candidateAnswerRepository,
                           CandidacyRepository candidacyRepository,
                           CandidateProfileRepository candidateProfileRepository,
                           QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
        this.candidateAnswerRepository = candidateAnswerRepository;
        this.candidacyRepository = candidacyRepository;
        this.candidateProfileRepository = candidateProfileRepository;
    }

    /**
     * Calculates matching results for all candidates of a committee.
     *
     * <p>Workflow:
     * <ol>
     *   <li>Load all candidacies for the committee</li>
     *   <li>Determine the number of questions in the system</li>
     *   <li>For each candidacy: load candidate profile and answers</li>
     *   <li>Include candidate only if answers are complete (size == question count and no null)</li>
     *   <li>Compute match percentage against the voter's answers</li>
     *   <li>Return list sorted by highest match first</li>
     * </ol>
     *
     * @param committeeId the committee identifier (e.g. "FSR1", "StuPa")
     * @param voterAnswers list of voter answers including weight flags
     * @return sorted list of {@link CandidateMatchingResult} (highest percentage first)
     * @throws RuntimeException if a candidacy has no corresponding {@link CandidateProfile}
     */
    public List<CandidateMatchingResult> calculateForCommittee(String committeeId,
                                                               List<MatchingRequest> voterAnswers){

        List<Candidacy> candidacies = candidacyRepository.findByCommitteeId(committeeId);

        // Anzahl der erwarteten Antworten
        List<Question> allquestions = this.questionRepository.findAll();
        int questionsCount = allquestions.size();

        //Resultat-Liste
        List<CandidateMatchingResult> matchingResults = new ArrayList<>();

        //Schleife, die nun durch alle Candidacies des Committees geht
        for (Candidacy candidacy : candidacies) {

            //über User -> Candidate für Methodenaufruf von calculateMatchPercentage
            User user = candidacy.getUser();
            CandidateProfile candidate = candidateProfileRepository
                                        .findByUser(user)
                                        .orElseThrow(() -> new RuntimeException("Kein CandidateProfile zu diesem User:" + user.getUserId()));

            // Antworten des Kandidaten laden
            List<CandidateAnswer> candidateAnswers = candidateAnswerRepository.findByCandidate(candidate);

            // Prüfung: Alle Fragen beantwortet UND keine null-Antworten
            boolean hasCompleteAnswers = candidateAnswers.size() == questionsCount && candidateAnswers.stream()
                            .allMatch(a -> a.getSelectedOption() != null);

            // Nur Kandidaten mit vollständigen Antworten ins Matching einbeziehen
            if (!hasCompleteAnswers) {
                continue; // Kandidat überspringen
            }

            //Berechnung des Matches von Candidate und Voter
            double percentage = calculateMatchPercentage( voterAnswers, candidate);

            CandidacyList list = candidacy.getCandidacyList();

            //Erstellung eines CandidateMatchingResult für Candidate, hinzufügen zu Resultat-Liste
            CandidateMatchingResult matchResult = new CandidateMatchingResult(
                                                candidate.getId(),
                                                candidate.getFirstname(),
                                                candidate.getLastname(),
                                                candidate.getAboutMe(),
                                                candidate.getFacultyId(),
                                                list != null ? list.getListId()    : null,
                                                list != null ? list.getNumber()    : null,
                                                list != null ? list.getListName()  : null,
                                                candidacy.getCommitteeId(),
                                                percentage);

            matchingResults.add(matchResult);
        }

        // sortierung höchste übereinstimmung zuerst 
        matchingResults.sort(
            Comparator.comparingDouble(CandidateMatchingResult::matchingPercentage).reversed()
    );
        return matchingResults;
    }

    //Matching-Logik für Voter und einem Candidate aus dem derzeitigen Gremium
    /**
     * Calculates the matching percentage between a voter answer set and one candidate.
     *
     * <p>Skipped voter answers are ignored. For each considered question:
     * <ul>
     *   <li>Compute squared distance between voter option and candidate option</li>
     *   <li>If voter marked the question as weighted, multiply distance by 2</li>
     *   <li>Compute the maximal possible distance relative to the voter's choice</li>
     * </ul>
     *
     * <p>The final percentage is computed as:
     * {@code (1 - actualDistance / maxDistance) * 100}.
     *
     * @param voterAnswers list of voter answers (may include skipped answers)
     * @param candidate candidate profile whose stored answers are used
     * @return match percentage in range 0..100 (0 if max distance is 0)
     */
    public double calculateMatchPercentage(List<MatchingRequest> voterAnswers, CandidateProfile candidate) {
        // Laden der Antworten des übergebenen Kandidaten
        List<CandidateAnswer> candidateAnswers = candidateAnswerRepository.findByCandidate(candidate);
        Map<Long, AnswerOption> candidateAnswerMap = createAnswerMap(candidateAnswers);

        // Es werden parallel die tatsächlichen und maximal möglichen Abstände berechnet
        // Entspricht der Summe aller quadrierten Abstände
        double sumOfSquaredDistances = 0.0;
        double maxPossibleDistance = 0.0;


        for (MatchingRequest voterAnswer : voterAnswers) {
            if (voterAnswer.selectedOption() == AnswerOption.FRAGE_UEBERSPRINGEN) {
                continue; // Übersprungene Fragen gehen nicht in die Berechnung ein -> weiter zum nächsten VoterAnswer
            }

            AnswerOption candidateAnswer = candidateAnswerMap.get(voterAnswer.questionId());
            if (candidateAnswer == null) {
                continue; // Nur der Form halber, wir gehen davon aus, dass alle Fragen beantwortet wurden
            }

            // Tatsächlichen Abstand berechnen, hier wird auch Gewichtung berücksichtigt
            double distance = calculateSquaredDistance(voterAnswer, candidateAnswer);
            sumOfSquaredDistances += distance;

            // Maximal möglichen Abstand von Antwort des Wählenden berechnen
            double maxDistance = calculateMaxSquaredDistance(voterAnswer);
            maxPossibleDistance += maxDistance;
        }

        // Vermeidung Division durch Null, eher unwahrscheinlich
        if (maxPossibleDistance == 0) {
            return 0.0;
        }

        // Wurzel für euklidische Distanz
        double actualDistance = Math.sqrt(sumOfSquaredDistances);
        double maxDistance = Math.sqrt(maxPossibleDistance);

        // Umwandlung in Prozentwert durch Vergleich mit maximal möglichem Abstand
        return (1 - (actualDistance / maxDistance)) * 100;
    }

    // Umwandlung CandidateAnswers in Map für schnellen Zugriff via questionId

    /**
     * Creates a map from questionId to the candidate's selected option for fast lookup.
     *
     * @param candidateAnswers list of answers of a candidate
     * @return map keyed by questionId with the selected {@link AnswerOption}
     */
    private Map<Long, AnswerOption> createAnswerMap(List<CandidateAnswer> candidateAnswers) {
        return candidateAnswers.stream()
                .collect(Collectors.toMap(
                        answer -> answer.getQuestion().getQuestionId(),
                        CandidateAnswer::getSelectedOption
                ));
    }


    // Berechnung des quadrierten Abstands zwischen Wählerantwort und Kandidatenantwort zu EINER Aussage
    /**
     * Calculates the squared distance between a voter's answer and a candidate's answer
     * for a single question.
     *
     * <p>The option values are taken from {@link AnswerOption#getValue()} and the absolute
     * difference is squared. If the voter marked the question as weighted, the squared
     * difference is multiplied by 2.
     *
     * @param voterAnswer the voter's answer for one question (including weight flag)
     * @param candidateAnswer the candidate's selected option for the same question
     * @return squared distance (weighted if applicable)
     */
    private double calculateSquaredDistance(MatchingRequest voterAnswer, AnswerOption candidateAnswer) {
        // Auslesen Zahlenwerte
        int voterValue = voterAnswer.selectedOption().getValue();
        int candidateValue = candidateAnswer.getValue();

        // Absolute Differenz berechnen, anschließend quadrieren
        double squaredDifference = Math.pow(Math.abs(voterValue - candidateValue), 2);

        // Ggf. Gewichtung berücksichtigen
        return voterAnswer.isWeighted() ? squaredDifference * 2 : squaredDifference;
    }

    // Berechnung des maximal möglichen quadrierten Abstands für eine Wählerantwort
    /**
     * Calculates the maximal possible squared distance for one voter answer.
     *
     * <p>The maximum distance is computed relative to the voter's position on the scale.
     * Example: voterValue = 1, maximal distance to 4 is 3.
     * This is then squared and multiplied by 2 if weighted.
     *
     * @param voterAnswer the voter's answer for one question
     * @return maximal possible squared distance (weighted if applicable)
     */
    private double calculateMaxSquaredDistance(MatchingRequest voterAnswer) {
        // Maximaler Abstand wird relativ zur Position des Voters berechnet
        // z.B. Voter Antwort ist 1(STIMME_NICHT_ZU) -> max Abstand zu 4(STIMME_VOLL_ZU) ist 3

        int voterValue = voterAnswer.selectedOption().getValue();

        int maxDistance = Math.max(voterValue, 4 - voterValue);
        double maxSquaredDifference = Math.pow(maxDistance, 2);

        // Gewichtung auch hier berücksichtigen
        return voterAnswer.isWeighted() ? maxSquaredDifference * 2 : maxSquaredDifference;
    }
}
