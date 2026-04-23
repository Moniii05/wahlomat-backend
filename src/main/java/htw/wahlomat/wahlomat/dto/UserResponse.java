package htw.wahlomat.wahlomat.dto;

import htw.wahlomat.wahlomat.model.Role;
/**
 * Response DTO representing a user without sensitive data.
 *
 * <p>This is a safe representation of {@code User} for API responses
 * and does not expose passwords or internal fields.</p>
 *
 * @param userId unique identifier of the user
 * @param email email address of the user
 * @param role role of the user
 */
public record UserResponse(
        Long userId,
        String email,
        Role role
) {}