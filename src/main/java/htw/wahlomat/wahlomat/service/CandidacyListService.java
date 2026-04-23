package htw.wahlomat.wahlomat.service;

import htw.wahlomat.wahlomat.dto.CandidacyListRequest;
import htw.wahlomat.wahlomat.model.CandidacyList;
import htw.wahlomat.wahlomat.model.profilePage.Candidacy;
import htw.wahlomat.wahlomat.repository.CandidacyListRepository;
import htw.wahlomat.wahlomat.repository.profilePage.CandidacyRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
/**
 * Service layer for managing {@link CandidacyList} entities.
 *
 * <p>Provides functionality for:
 * <ul>
 *   <li>Retrieving all lists</li>
 *   <li>Filtering lists by committee ID</li>
 *   <li>Creating new lists</li>
 *   <li>Deleting lists including associated {@link Candidacy} entries</li>
 * </ul>
 *
 * <p>This service acts as the business layer between controller and repository.
 */
@Service
public class CandidacyListService
{
    private final CandidacyListRepository candidacyListRepository;
    private final CandidacyRepository candidacyRepository;

    /**
     * Creates a new {@code CandidacyListService}.
     *
     * @param candidacyListRepository repository for accessing and persisting candidacy lists
     * @param candidacyRepository repository for accessing and deleting candidacies
     */
    public CandidacyListService(CandidacyListRepository candidacyListRepository, CandidacyRepository candidacyRepository) {
        this.candidacyRepository = candidacyRepository;
        this.candidacyListRepository = candidacyListRepository;
    }

    /**
     * Returns all candidacy lists stored in the database.
     *
     * @return list of all {@link CandidacyList} entities
     */
    public List<CandidacyList> getAllCandidacyLists() {
        return candidacyListRepository.findAll();
    }

    /**
     * Returns all candidacy lists belonging to a specific committee.
     *
     * @param id committee identifier (e.g. "FSR1")
     * @return list of {@link CandidacyList} associated with the given committee ID
     */
    public List<CandidacyList> findAllByCommitteeID(String id) {
        return candidacyListRepository.findAllByCommitteeId(id);
    }

    /**
     * Creates and persists a new {@link CandidacyList}.
     *
     * @param request DTO containing list number, name and committee ID
     * @return the persisted {@link CandidacyList} entity
     */
    public CandidacyList createList(CandidacyListRequest request) {
        CandidacyList list = new CandidacyList(
                request.number(),
                request.listName(),
                request.committeeId()
        );
        return candidacyListRepository.save(list);
    }

    /**
     * Retrieves a {@link CandidacyList} by its ID.
     *
     * @param id ID of the list
     * @return optional containing the list if found, otherwise empty
     */
    public Optional<CandidacyList> getListById(Long id) {
        return candidacyListRepository.findById(id);
    }

    /**
     * Deletes a {@link CandidacyList} by its ID.
     *
     * <p>If candidacies are associated with the list, they are deleted first
     * to prevent foreign key constraint violations.
     *
     * @param listId ID of the list to delete
     * @throws RuntimeException if the list does not exist
     */
    @Transactional
    public void deleteCandidacyListById (Long listId)
    {
        //Zuerst wird geprüft, ob die Liste mit der listId existiert
        CandidacyList deleteList = this.candidacyListRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("CandidacyList with listId " + listId + " does not exist"));


        // Abfrage, ob Kandidaturen zu der Liste existieren + löschen dieser Kandidaturen
        List<Candidacy> existingCandidacies = this.candidacyRepository.findByCandidacyList(deleteList);
        System.out.println("Found " + existingCandidacies.size() + " candidacies for listId " + listId);

        if (!existingCandidacies.isEmpty()) {
            this.candidacyRepository.deleteAll(existingCandidacies);  // Batch-Löschen
        }

        //jetzt wird Liste final anhand listId gelöscht
        this.candidacyListRepository.deleteById(listId);
    }
}
