// User Repository für SQL Anfragen an DB

package htw.wahlomat.wahlomat.repository;

import htw.wahlomat.wahlomat.model.Role;
import htw.wahlomat.wahlomat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

// hiermit werden uns CRUD -Methoden als abstrakte Methoden zur Verfügung gestellt (von JpaRepository)
/**
 * Repository interface for accessing {@link User} entities.
 *
 * <p>Extends {@link JpaRepository} to provide standard CRUD operations
 * such as save, findAll, findById, and deleteById.
 *
 * <p>Additional query methods are defined for application-specific
 * lookup operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>
{
    // wir können findBy nehmen und irgendein Attribut aus Model nehmen
    /**
     * Finds a user by its email address.
     *
     * @param email the email address of the user
     * @return an {@link Optional} containing the user if found,
     *         otherwise {@link Optional#empty()}
     */
    Optional <User> findByEmail (String email); // das erste gefunde Objekte wird zurückgegeben

    /**
     * Checks whether a user with the given email exists.
     *
     * @param email the email to check
     * @return true if a user with this email exists, otherwise false
     */
    boolean existsByEmail (String email);

    /**
     * Checks whether a user exists with the given email and role.
     *
     * @param email the email address
     * @param role the role of the user (e.g. ADMIN, CANDIDATE)
     * @return true if such a user exists, otherwise false
     */
    boolean existsByEmailAndRole(String email, Role role);

    // US5 kandidatenerstellung
    List<User> findByRole(Role role); // eg CANDIDATE


}
