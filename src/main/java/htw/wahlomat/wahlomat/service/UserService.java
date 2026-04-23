package htw.wahlomat.wahlomat.service;

import htw.wahlomat.wahlomat.dto.UserResponse;
import htw.wahlomat.wahlomat.model.User;
import htw.wahlomat.wahlomat.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * Service responsible for user-related read operations.
 *
 * <p>This service provides read-only access to {@link User} entities
 * and maps them to {@link UserResponse} DTOs before returning them
 * to the controller layer.
 *
 * <p>Sensitive fields such as passwords are never exposed.
 */
@Service
public class UserService {

        private final UserRepository userRepository;

    /**
     * Creates a new {@link UserService}.
     *
     * @param userRepository repository used for accessing user data
     */
        public UserService(UserRepository userRepository) {
            this.userRepository = userRepository;
        }

        // Get all Users als UserResponse-Liste

    /**
     * Retrieves all users from the database.
     *
     * <p>Each {@link User} entity is mapped to a {@link UserResponse}
     * to ensure that only safe and relevant information is exposed.
     *
     * @return list of all users as {@link UserResponse} objects
     */
        public List<UserResponse> getAllUsers() {
            List<User> users = this.userRepository.findAll();
            return users.stream()
                    .map(user -> new UserResponse(
                            user.getUserId(),
                            user.getEmail(),
                            user.getRole()
                    ))
                    .toList();
        }

        // Get one User by ID als UserResponse
    /**
     * Retrieves a single user by its ID.
     *
     * <p>If the user exists, it is mapped to a {@link UserResponse}.
     * If not, an empty {@link Optional} is returned.
     *
     * @param id the unique identifier of the user
     * @return optional containing the {@link UserResponse} if found,
     *         otherwise {@link Optional#empty()}
     */
        public Optional<UserResponse> getUserById(Long id) {
            Optional<User> user = this.userRepository.findById(id);
            return user.map(foundUser -> new UserResponse(
                    foundUser.getUserId(),
                    foundUser.getEmail(),
                    foundUser.getRole()
            ));
        }
    }