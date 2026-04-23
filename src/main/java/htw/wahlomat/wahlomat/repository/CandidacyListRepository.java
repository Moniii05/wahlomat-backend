package htw.wahlomat.wahlomat.repository;

import htw.wahlomat.wahlomat.model.CandidacyList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link CandidacyList} entities.
 *
 * <p>Provides access to list-related database operations
 * for election committees.
 */
@Repository
public interface CandidacyListRepository extends JpaRepository<CandidacyList, Long>
{

    /**
     * Retrieves all lists belonging to a specific committee.
     *
     * @param committeeId the identifier of the committee
     * @return list of candidacy lists for the committee
     */
    List<CandidacyList> findAllByCommitteeId(String committeeId);

    // Liste mit bestimmten Namen laden
    /**
     * Finds a list by its name.
     *
     * @param listName the name of the list
     * @return optional containing the list if found
     */
    Optional<CandidacyList> findByListName(String listName);
}