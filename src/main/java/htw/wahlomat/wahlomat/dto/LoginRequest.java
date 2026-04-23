package htw.wahlomat.wahlomat.dto;

import jakarta.validation.constraints.NotBlank;
/**
 * Request DTO for user login.
 *
 * @param email email address of the user
 * @param password plain text password provided during login
 */
public record LoginRequest(

        @NotBlank(message = "HTW-E-Mail ist erforderlich")
        String email,

        @NotBlank(message = "Passwort ist erforderlich")
        String password
){}