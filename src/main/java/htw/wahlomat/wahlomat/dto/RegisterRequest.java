package htw.wahlomat.wahlomat.dto;
import jakarta.validation.constraints.*;

import java.util.List;


/**
 * Request DTO for candidate registration.
 *
 * <p>Contains authentication credentials, profile fields, and the initial list of candidacies.</p>
 *
 * @param email candidate email address
 * @param password plain text password (min. length 6)
 * @param firstname candidate first name
 * @param lastname candidate last name
 * @param facultyId faculty identifier
 * @param aboutMe optional candidate description (max 500 characters)
 * @param candidacies initial candidacies of the candidate (must not be empty)
 */
public record RegisterRequest (
        @NotBlank(message = "Email darf nicht leer sein")
        @Email(message = "Ungültige Email-Adresse")
        String email,

        @NotBlank(message = "Passwort darf nicht leer sein")
        @Size(min = 6, message = "Passwort muss mindestens 6 Zeichen lang sein")
        String password,

        @NotBlank(message = "Vorname darf nicht leer sein")
        String firstname,

        @NotBlank(message = "Nachname darf nicht leer sein")
        String lastname,

        @NotNull
        Long facultyId,

        @Size(max = 500, message = "About Me darf maximal 500 Zeichen lang sein")
        String aboutMe,

        @NotEmpty(message = "Candidacies Liste darf nicht leer sein")
        List<CandidacyRequest> candidacies

) {}
