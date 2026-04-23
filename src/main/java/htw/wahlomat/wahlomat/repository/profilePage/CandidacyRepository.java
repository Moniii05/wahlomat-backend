package htw.wahlomat.wahlomat.repository.profilePage;

import htw.wahlomat.wahlomat.model.CandidacyList;
import htw.wahlomat.wahlomat.model.User;
import htw.wahlomat.wahlomat.model.profilePage.Candidacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Candidacy} entities.
 *
 * <p>Provides access to all candidacy-related database operations,
 * including user-based, list-based, and committee-based queries.
 */
@Repository // optional 
public interface CandidacyRepository extends JpaRepository<Candidacy, Long> {

    // Alle Candidacies eines Users
    /**
     * Retrieves all candidacies of a specific user.
     *
     * @param user the user entity
     * @return list of candidacies belonging to the user
     */
    List<Candidacy> findByUser(User user);

    // Alle Candidacies für eine bestimmte Liste
    /**
     * Retrieves all candidacies associated with a specific list.
     *
     * @param CandidacyList the list entity
     * @return list of candidacies for the given list
     */
    List<Candidacy> findByCandidacyList(CandidacyList CandidacyList);

    // Alle Candidacies für ein bestimmtes Gremium
    /**
     * Retrieves all candidacies for a specific committee.
     *
     * @param committeeId the identifier of the committee
     * @return list of candidacies for the committee
     */
    List<Candidacy> findByCommitteeId(String committeeId);

    // prüfen, ob user in einem Gremium schon kandidiert
    /**
     * Checks whether a user is already a candidate
     * for a specific committee.
     *
     * @param user the user entity
     * @param committeeId the committee identifier
     * @return true if such a candidacy exists, otherwise false
     */
    boolean existsByUserAndCommitteeId(User user, String committeeId);

    // einzelne Candidacy von users (für updaten/validieren)
    // Suche über CandidacyId reicht aus (kein User), da eindeutig
    /**
     * Finds a candidacy by its unique identifier.
     *
     * @param candidacyId the id of the candidacy
     * @return optional containing the candidacy if found
     */
    Optional<Candidacy> findByCandidacyId(Long candidacyId);

    // holt genau eine Kandidatur eines Users von bestimmtem Gremium 
    // falls nicht gibt --> empty
    /**
     * Retrieves the candidacy of a user for a specific committee.
     *
     * @param user the user entity
     * @param committeeId the committee identifier
     * @return optional containing the candidacy if present
     */
    Optional<Candidacy> findByUserAndCommitteeId(User user, String committeeId);

    // löscht alle Candidacies von User
    /**
     * Deletes all candidacies belonging to a specific user.
     *
     * @param user the user entity
     */
    void deleteByUser(User user); // optional, sonst deleteAll(findByUserId(...))
}
