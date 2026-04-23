// Service mit Methoden für Registrierung und Login von Usern
// verwendet UserRepository für DB-Zugriffe und JwtUtil (s. security) für Token-Generierung

package htw.wahlomat.wahlomat.service;

import htw.wahlomat.wahlomat.dto.ChangePasswordRequest;
import htw.wahlomat.wahlomat.dto.CandidacyRequest;
import htw.wahlomat.wahlomat.dto.LoginRequest;
import htw.wahlomat.wahlomat.dto.RegisterRequest;
import htw.wahlomat.wahlomat.model.CandidacyList;
import htw.wahlomat.wahlomat.model.Role;
import htw.wahlomat.wahlomat.model.admin.CandidateInvite;
import htw.wahlomat.wahlomat.model.profilePage.CandidateProfile;
import htw.wahlomat.wahlomat.model.profilePage.Candidacy;
import jakarta.validation.Valid;
import htw.wahlomat.wahlomat.repository.CandidacyListRepository;
import htw.wahlomat.wahlomat.repository.CandidateInviteRepository;
import htw.wahlomat.wahlomat.repository.UserRepository;
import htw.wahlomat.wahlomat.repository.profilePage.CandidacyRepository;
import htw.wahlomat.wahlomat.repository.profilePage.CandidateProfileRepository;
import htw.wahlomat.wahlomat.dto.AuthResponse;
import htw.wahlomat.wahlomat.model.User;
import htw.wahlomat.wahlomat.security.JwtUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for user authentication and account-related actions.
 *
 * <p>Provides:
 * <ul>
 *   <li>Candidate registration (only with a valid invitation)</li>
 *   <li>Login for all users (ADMIN and CANDIDATE)</li>
 *   <li>Password change for the currently authenticated user</li>
 * </ul>
 *
 * <p>Uses {@link UserRepository} for persistence and {@link JwtUtil} for JWT creation.
 * Registration writes multiple entities (User, CandidateProfile, Candidacy) and is transactional.
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CandidateInviteRepository candidateInviteRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final CandidacyRepository candidacyRepository;
    private final CandidacyListRepository candidacyListRepository;

    /**
     * Creates a new {@code AuthService}.
     *
     * @param userRepository repository for user persistence and lookup
     * @param passwordEncoder encoder used to hash and verify passwords
     * @param jwtUtil utility for generating JWT tokens
     * @param candidateInviteRepository repository for candidate invitations
     * @param candidateProfileRepository repository for candidate profiles
     * @param candidacyRepository repository for candidacies
     * @param candidacyListRepository repository for candidacy lists
     */
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            CandidateInviteRepository candidateInviteRepository,
            CandidateProfileRepository candidateProfileRepository,
            CandidacyRepository candidacyRepository,
            CandidacyListRepository candidacyListRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.candidateInviteRepository = candidateInviteRepository;
        this.candidateProfileRepository = candidateProfileRepository;
        this.candidacyRepository = candidacyRepository;
        this.candidacyListRepository = candidacyListRepository;
    }

    /**
     * Registers a new candidate user.
     *
     * <p>Workflow:
     * <ol>
     *   <li>Validate that an invitation exists for the email</li>
     *   <li>Create a new {@link User} with role {@link Role#CANDIDATE}</li>
     *   <li>Create a {@link CandidateProfile} (firstname, lastname, facultyId, aboutMe)</li>
     *   <li>Create one {@link Candidacy} per entry in {@code request.candidacies()}</li>
     *   <li>Delete the invitation after successful registration</li>
     *   <li>Return an {@link AuthResponse} containing a JWT token</li>
     * </ol>
     *
     * <p>This method is transactional: if any step fails, all database changes are rolled back.
     *
     * @param request registration request containing credentials, profile data and candidacies
     * @return authentication response containing JWT token, email, role and message
     * @throws RuntimeException if no invitation exists, email is already registered,
     *                          a list does not exist, or another validation step fails
     */
    @Transactional // Bei Fehler werden ALLE DB-Änderungen rückgängig gemacht
    public AuthResponse register(RegisterRequest request) {

        logger.info("=== REGISTRIERUNG GESTARTET ===");
        logger.info("Email: {}", request.email());

        // ============================================================
        // SCHRITT 1: Einladung prüfen
        // - Es muss eine Einladung für diese Email existieren
        // - Die Einladung muss noch gültig sein (nicht abgelaufen, Status INVITED)
        // ============================================================
        logger.info("Schritt 1: Prüfe ob Einladung existiert...");

        // Email in Kleinbuchstaben umwandeln (Einladungen werden lowercase gespeichert)
        String emailLowerCase = request.email().toLowerCase();

        // Einladung in der Datenbank suchen
        CandidateInvite invite = candidateInviteRepository.findByEmail(emailLowerCase)
                .orElseThrow(() -> {
                    logger.error("Keine Einladung gefunden für: {}", emailLowerCase);
                    return new RuntimeException("Keine gültige Einladung für diese Email-Adresse vorhanden.");
                });



        // ============================================================
        // SCHRITT 2: Neuen User erstellen
        // - Speichert Email und verschlüsseltes Passwort
        // - Rolle wird automatisch auf CANDIDATE gesetzt
        // ============================================================
        logger.info("Schritt 2: Erstelle neuen User...");

        // Prüfen ob Email bereits als User existiert (Sicherheitscheck)
        if (userRepository.existsByEmail(emailLowerCase)) {
            logger.error("Email bereits als User registriert: {}", emailLowerCase);
            throw new RuntimeException("Diese Email-Adresse ist bereits registriert.");
        }

        // Neues User-Objekt erstellen
        User user = new User();
        user.setEmail(emailLowerCase);
        user.setPassword(passwordEncoder.encode(request.password())); // Passwort verschlüsseln!
        user.setRole(Role.CANDIDATE);

        // User in der Datenbank speichern (generiert automatisch userId)
        User savedUser = userRepository.save(user);
        logger.info("User erstellt mit ID: {}", savedUser.getUserId());

        // ============================================================
        // SCHRITT 3: CandidateProfile erstellen
        // - Speichert persönliche Daten (Name, Fakultät, About Me)
        // - Verknüpft mit dem gerade erstellten User
        // ============================================================
        logger.info("Schritt 3: Erstelle CandidateProfile...");

        // Neues Profil mit allen Daten aus dem Request erstellen
        CandidateProfile profile = new CandidateProfile(
                savedUser,                    // Referenz auf den User
                request.firstname(),          // Vorname
                request.lastname(),           // Nachname
                request.facultyId(),          // Fachbereich
                request.aboutMe()             // Über mich Text (kann null sein)
        );

        // Profil in der Datenbank speichern
        candidateProfileRepository.save(profile);
        logger.info("CandidateProfile erstellt für User: {}", savedUser.getUserId());

        // ============================================================
        // SCHRITT 4: Kandidaturen erstellen
        // - Für jede Kandidatur aus der Liste wird ein Eintrag erstellt
        // - Verknüpft User mit Gremium und Liste
        // ============================================================
        logger.info("Schritt 4: Erstelle {} Kandidatur(en)...", request.candidacies().size());

        // Durch alle Kandidaturen aus dem Request iterieren
        for (CandidacyRequest candidacyRequest : request.candidacies()) {
            logger.info("Erstelle Kandidatur für Gremium: {}, Liste: {}",
                    candidacyRequest.committeeId(), candidacyRequest.listId());

            // Liste aus der Datenbank laden (muss existieren)
            CandidacyList list = candidacyListRepository.findById(candidacyRequest.listId())
                    .orElseThrow(() -> {
                        logger.error("Liste nicht gefunden: {}", candidacyRequest.listId());
                        return new RuntimeException("Liste mit ID " + candidacyRequest.listId() + " existiert nicht.");
                    });

            // Neue Kandidatur erstellen und speichern
            Candidacy candidacy = new Candidacy(
                    savedUser,                        // Referenz auf den User
                    candidacyRequest.committeeId(),   // Gremium-ID (z.B. "FSR1")
                    list                              // Referenz auf die gewählte Liste
            );

            candidacyRepository.save(candidacy);
            logger.info("Kandidatur erstellt: Gremium={}, Liste={}",
                    candidacyRequest.committeeId(), list.getListId());
        }

        logger.info("Alle {} Kandidatur(en) erfolgreich erstellt.", request.candidacies().size());

        // ============================================================
        // SCHRITT 5: Einladung löschen
        // - Einladung wird aus der Datenbank entfernt
        // - Alternativ könnte man den Status auf REGISTERED setzen
        // ============================================================
        logger.info("Schritt 5: Lösche Einladung...");

        candidateInviteRepository.delete(invite);
        logger.info("Einladung gelöscht für: {}", emailLowerCase);

        // ============================================================
        // SCHRITT 6: AuthResponse erstellen und zurückgeben
        // - Generiert JWT Token für automatischen Login
        // - Gibt alle relevanten Daten ans Frontend zurück
        // ============================================================
        logger.info("Schritt 6: Erstelle AuthResponse...");

        // JWT Token generieren (für automatischen Login nach Registrierung)
        // Parameter: userId, role, email (siehe JwtUtil.generateToken)
        String token = jwtUtil.generateToken(
                savedUser.getUserId(),
                savedUser.getRole().name(),
                savedUser.getEmail()
        );

        logger.info("=== REGISTRIERUNG ERFOLGREICH ABGESCHLOSSEN ===");
        logger.info("User {} wurde erfolgreich registriert.", savedUser.getEmail());

        // Response mit Token und User-Daten zurückgeben
        return new AuthResponse(
                token,
                savedUser.getEmail(),
                savedUser.getRole(),
                "Registrierung erfolgreich"
        );
    }


    /**
     * Authenticates a user (ADMIN or CANDIDATE) using email and password.
     *
     * <p>On success, returns an {@link AuthResponse} containing a JWT token.
     *
     * @param request login request containing email and password
     * @return authentication response containing JWT token, email, role and message
     * @throws UsernameNotFoundException if the email does not exist or the password is invalid
     */
    public AuthResponse login(@Valid LoginRequest request) {

        logger.info("Login attempt for: {}", request.email());

        // 1. User finden
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    logger.error("User not found: {}", request.email());
                    return new UsernameNotFoundException("Ungültige Anmeldedaten");
                });

        logger.info("User found: {} with role: {}", user.getEmail(), user.getRole());
        logger.info("Stored password hash: {}", user.getPassword().substring(0, 10) + "...");

        boolean passwordMatches = passwordEncoder.matches(request.password(), user.getPassword());
        logger.info("Password matches: {}", passwordMatches);


        // 2. Passwort überprüfen
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UsernameNotFoundException("Ungültige Anmeldedaten");
        }

        logger.info("Login successful for: {}", user.getEmail());

        // 3. Token mit Rolle generieren
        String token = jwtUtil.generateToken(
                user.getUserId(),
                user.getRole().name(),
                user.getEmail());

        // 4. Response mit Rolle zurückgeben
        return new AuthResponse(
                token,
                user.getEmail(),
                user.getRole(),
                //user.getUserId(),
                "Login erfolgreich"
        );
    }

    /**
     * Changes the password for the currently authenticated user.
     *
     * <p>The method reads the authenticated principal from the {@link SecurityContextHolder}.
     * If the principal is a Hibernate proxy, it is unwrapped.
     * The current password must match before the password is updated.
     *
     * @param request password change request containing currentPassword and newPassword
     * @throws RuntimeException if the user is not authenticated, the principal type is invalid,
     *                          or the current password does not match
     */
    public void changePassword(ChangePasswordRequest request)
    {
        // 1. Eingeloggten User aus Security Context holen
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getPrincipal() == null)
        {
            throw new RuntimeException("Nicht authentifiziert.");
        }

        Object principal = auth.getPrincipal();

        // 2. User-Objekt extrahieren (kann Hibernate Proxy sein)
        User user;
        if (principal instanceof org.hibernate.proxy.HibernateProxy)
        {
            user = (User) ((org.hibernate.proxy.HibernateProxy) principal)
                    .getHibernateLazyInitializer()
                    .getImplementation();
        }
        else if (principal instanceof User)
        {
            user = (User) principal;
        }
        else
        {
            throw new RuntimeException("Ungültige Authentifizierung.");
        }

        // 3. Aktuelles Passwort prüfen
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword()))
        {
            throw new RuntimeException("Aktuelles Passwort ist falsch.");
        }

        // 4. Neues Passwort hashen und setzen
        user.setPassword(passwordEncoder.encode(request.newPassword()));

        // 5. User speichern
        userRepository.save(user);
    }
}



