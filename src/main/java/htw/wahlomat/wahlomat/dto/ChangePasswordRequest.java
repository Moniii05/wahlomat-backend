package htw.wahlomat.wahlomat.dto;

import jakarta.validation.constraints.NotNull;
/**
 * Request DTO for changing a user's password.
 *
 * <p>The current password may be {@code null} if a "forgot password" flow
 * is implemented in the future.</p>
 *
 * @param currentPassword current password of the user (may be {@code null})
 * @param newPassword new password to set (must not be {@code null})
 */
public record ChangePasswordRequest(
        String currentPassword, // kann null sein, sofern wir Passwort vergessen implementieren
        @NotNull String newPassword
) {}
