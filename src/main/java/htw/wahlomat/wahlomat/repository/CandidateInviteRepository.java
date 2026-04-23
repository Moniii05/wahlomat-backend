package htw.wahlomat.wahlomat.repository;

import htw.wahlomat.wahlomat.model.admin.CandidateInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository interface for managing {@link CandidateInvite} entities.
 *
 * <p>Provides CRUD operations and custom lookup methods
 * for handling candidate invitation logic.
 */
public interface CandidateInviteRepository extends JpaRepository<CandidateInvite, Long> {

    /**
     * Finds a candidate invitation by email address.
     *
     * <p>The email should be stored and searched in lowercase format.
     *
     * @param emailLowerCase the normalized (lowercase) email address
     * @return an {@link Optional} containing the invite if found,
     *         otherwise {@link Optional#empty()}
     */
    Optional<CandidateInvite> findByEmail(String emailLowerCase);
}
