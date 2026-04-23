package htw.wahlomat.wahlomat.repository.profilePage;

import htw.wahlomat.wahlomat.model.User;
import htw.wahlomat.wahlomat.model.profilePage.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// DB zugriff (JPA= generiert Meth. wie findId,save,deletebyId)


/**
 * Repository interface for managing {@link CandidateProfile} entities.
 *
 * <p>Handles persistence operations for candidate profiles.
 * Each {@link User} is associated with exactly one {@link CandidateProfile}
 * (1:1 relationship).
 */
@Repository // optional
public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, Long> {
    /**
     * Retrieves the profile associated with a specific user.
     *
     * @param user the user entity
     * @return an {@link Optional} containing the candidate profile if found,
     *         otherwise {@link Optional#empty()}
     */
    Optional<CandidateProfile> findByUser(User user);

    /**
     * Checks whether a profile exists for the given user.
     *
     * @param user the user entity
     * @return true if a profile exists, otherwise false
     */
    boolean existsByUser(User user);

    // profil löschen, wenn User sein Profil entfernt
    /**
     * Deletes the profile associated with a specific user.
     *
     * @param user the user entity
     */
    void deleteByUser(User user);

}
