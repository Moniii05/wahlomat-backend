package htw.wahlomat.wahlomat.dto;

import htw.wahlomat.wahlomat.model.Role;

/**
 * Response DTO for authentication requests (login or registration).
 *
 * <p>Contains the issued JWT token (if successful) and basic user information.</p>
 *
 * @param token JWT token issued after successful authentication, or {@code null} if authentication failed
 * @param email email address of the authenticated user, or {@code null} if authentication failed
 * @param role role of the authenticated user (e.g. ADMIN or CANDIDATE), or {@code null} if authentication failed
 * @param message success or error message describing the authentication result
 */

public record AuthResponse(
        String token,
        String email,
        Role role,
        //Long userId,
        String message

){}