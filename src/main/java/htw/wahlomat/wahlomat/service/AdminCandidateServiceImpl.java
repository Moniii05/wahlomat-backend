
//Service für die Kandidatenverwaltung durch den Admin (invite Methode, etc)
package htw.wahlomat.wahlomat.service;

import htw.wahlomat.wahlomat.dto.admin.InviteCandidateRequest;
import htw.wahlomat.wahlomat.dto.admin.InviteCandidateResponse;
import htw.wahlomat.wahlomat.dto.admin.RegisteredCandidateResponse;
import htw.wahlomat.wahlomat.model.User;
import htw.wahlomat.wahlomat.model.admin.CandidateInvite;
import htw.wahlomat.wahlomat.repository.QuestionRepository;
import htw.wahlomat.wahlomat.model.admin.InviteStatus;
import htw.wahlomat.wahlomat.repository.UserRepository;
import htw.wahlomat.wahlomat.repository.CandidateAnswerRepository;
import htw.wahlomat.wahlomat.repository.profilePage.CandidateProfileRepository;
import htw.wahlomat.wahlomat.repository.profilePage.CandidacyRepository;
import htw.wahlomat.wahlomat.repository.CandidateInviteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import htw.wahlomat.wahlomat.dto.admin.BulkInviteResponse;
import htw.wahlomat.wahlomat.dto.admin.BulkInviteResponse.Result;

//import htw.wahlomat.wahlomat.dto.admin.RegisteredCandidateResponse;
import java.util.*;
import java.util.stream.Collectors;


import static org.springframework.http.HttpStatus.*;
// import org.springframework.lang.NonNull;

import htw.wahlomat.wahlomat.model.Role;

/**
 * Service implementation for administrative candidate management.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Create and renew candidate invitations (with a TTL of {@value #INVITE_TTL_DAYS} days)</li>
 *   <li>Bulk invitation handling with validation, deduplication and reporting</li>
 *   <li>Delete registered candidates including dependent data (profile, answers, candidacies)</li>
 *   <li>List invitations and registered candidates</li>
 * </ul>
 *
 * <p>All operations run within a transaction by default (class-level {@link Transactional}).
 */
@Service
@Transactional
public class AdminCandidateServiceImpl implements AdminCandidateService {

    /**
     * Time-to-live for invitations in days.
     * Invitations older than this threshold are treated as expired and may be renewed.
     */
    private static final long INVITE_TTL_DAYS = 90;

    private final UserRepository userRepo;
    private final CandidateInviteRepository inviteRepo;
    private final CandidateProfileRepository profileRepo;
    private final CandidacyRepository candidacyRepo;
    private final CandidateAnswerRepository answerRepo;
    private final QuestionRepository questionRepo;


    /**
     * Creates a new service instance.
     *
     * @param userRepo repository for users
     * @param inviteRepo repository for candidate invitations
     * @param profileRepo repository for candidate profiles
     * @param candidacyRepo repository for candidacies
     * @param answerRepo repository for candidate answers
     * @param questionRepo repository for questions (currently unused in this service)
     */
    public AdminCandidateServiceImpl(
            UserRepository userRepo,
            CandidateInviteRepository inviteRepo,
            CandidateProfileRepository profileRepo,
            CandidacyRepository candidacyRepo,
            CandidateAnswerRepository answerRepo,
            QuestionRepository questionRepo
    ) {
        this.userRepo = userRepo;
        this.inviteRepo = inviteRepo;
        this.profileRepo = profileRepo;
        this.candidacyRepo = candidacyRepo;
        this.answerRepo = answerRepo;
        this.questionRepo = questionRepo;
    }


    /**
     * Creates or renews an invitation for the given email address.
     *
     * <p>Behaviour:
     * <ul>
     *   <li>Email is normalized (trimmed and converted to lowercase)</li>
     *   <li>Only HTW domains are accepted</li>
     *   <li>If an invite already exists and is expired, it is renewed (status set to INVITED and timestamp updated)</li>
     *   <li>If no invite exists, a new invite is created</li>
     * </ul>
     *
     * @param req invitation request containing the candidate email
     * @return the created or updated invitation
     * @throws ResponseStatusException with HTTP 400 (BAD_REQUEST) if email is not a permitted HTW domain
     */
    @Override
    public InviteCandidateResponse invite(InviteCandidateRequest req) {
    final String emailLowerCase = normalize(req.email());
    assertHtwDomain(emailLowerCase);

    // gibt es invite?
    CandidateInvite invite = inviteRepo.findByEmail(emailLowerCase).orElse(null);

    if (invite != null) {
        // TTL abgelaufen? -> zeit aktualisieren
        if (isExpired(invite.getInvitedAt())) {
            invite.setStatus(InviteStatus.INVITED);
            invite.setInvitedAt(Instant.now());
        }
        return map(inviteRepo.save(invite));
    }

    // kein Invite vorhanden  
    CandidateInvite created = new CandidateInvite(emailLowerCase);
    created.setStatus(InviteStatus.INVITED);
    return map(inviteRepo.save(created));
}





    /**
     * Deletes a single registered candidate and associated data.
     *
     * <p>Deletion order:
     * <ol>
     *   <li>CandidateProfile (if present) and its answers</li>
     *   <li>Candidacies of the user</li>
     *   <li>User entity</li>
     * </ol>
     *
     * @param user candidate user to delete
     */
    @Override
    public void deleteCandidate(User user) {  // @NonNull

        profileRepo.findByUser(user).ifPresent(profile -> {
            answerRepo.deleteByCandidate(profile);
            profileRepo.delete(profile);
        });

        candidacyRepo.deleteByUser(user);
        userRepo.deleteById(user.getUserId());
    }



    /**
     * Deletes all registered candidates (Role.CANDIDATE) including their dependent data.
     *
     * <p>This is intended as a system reset for candidate-related data.
     */
    @Override
    public void deleteAllRegistered() {
    var candidates = userRepo.findByRole(Role.CANDIDATE); 
    for (var u : candidates) {
        deleteCandidate(u); // löscht Antworten, Candidacies, Profile, dann User
    }
}



    /**
     * Returns all invitations sorted by email address (case-insensitive).
     *
     * @return sorted list of invitation responses
     */
   @Override
    public List<InviteCandidateResponse> listInvites() {
    return inviteRepo.findAll().stream()
            .sorted(Comparator.comparing(CandidateInvite::getEmail, String.CASE_INSENSITIVE_ORDER))
            .map(this::map) 
            .toList();
}




    /**
     * Returns all registered candidates enriched with profile information if available.
     *
     * <p>Sorting:
     * <ol>
     *   <li>Last name (null-safe, case-insensitive)</li>
     *   <li>First name (null-safe, case-insensitive)</li>
     *   <li>Email (case-insensitive)</li>
     * </ol>
     *
     * @return sorted list of registered candidate responses
     */
    @Override
    @Transactional(readOnly = true)
    public List<RegisteredCandidateResponse> listRegistered() {
        var candidates = userRepo.findByRole(Role.CANDIDATE);

        var list = candidates.stream().map(u -> {
            var profOpt = profileRepo.findByUser(u);
            var first = profOpt.map(p -> p.getFirstname()).orElse(null);
            var last  = profOpt.map(p -> p.getLastname()).orElse(null);
            var facId = profOpt.map(p -> p.getFacultyId()).orElse(null);
            return new RegisteredCandidateResponse(u.getUserId(), u.getEmail(), first, last, facId);
        }).toList();

        // alphabetisch: Nachname, Vorname, E-Mail (null-safe, case-insensitive)
        return list.stream()
            .sorted(
                Comparator.comparing(
              RegisteredCandidateResponse::lastName,
             Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
             // nullsLast damit nich abbricht, wenn datensatz unvollständig 
    ).thenComparing(
        RegisteredCandidateResponse::firstName,
        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
    ).thenComparing(
        RegisteredCandidateResponse::email,
        String.CASE_INSENSITIVE_ORDER
    )
  )
  .toList(); }
 


    // helper 
    private static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private static void assertHtwDomain(String emailLowerCase) {
        // Beispiel-Regel (an eure echten Domains anpassen)
        if (!(emailLowerCase.endsWith("@htw-berlin.de") || emailLowerCase.endsWith("@student.htw-berlin.de"))) {
            throw new ResponseStatusException(BAD_REQUEST, "Nur HTW-Domains erlaubt.");
        }
    }

    private static boolean isExpired(Instant invitedAt) {
        Instant threshold = Instant.now().minus(INVITE_TTL_DAYS, ChronoUnit.DAYS);
        return invitedAt.isBefore(threshold);
    }


    
    private InviteCandidateResponse map(CandidateInvite i) {
        Long registeredUserId = null;
        if (i.getRegisteredUser() != null) {
            registeredUserId = i.getRegisteredUser().getUserId();
        }

        return new InviteCandidateResponse(
                i.getId(),
                i.getEmail(),
                i.getStatus(),
                i.getInvitedAt(),
                registeredUserId
        );
    }

   /*  private static Long getUserId(User u) {
        return u.getUserID();
    }
        */

    /**
     * Invites multiple candidates at once.
     *
     * <p>Processing rules:
     * <ul>
     *   <li>Input emails are normalized (trim + lowercase), empty values removed</li>
     *   <li>Duplicates are removed while keeping original order</li>
     *   <li>Only HTW domains are accepted (others are returned as FAILED)</li>
     *   <li>If a user already exists for an email, it is SKIPPED</li>
     *   <li>If an active invite exists (not expired and status INVITED), it is SKIPPED</li>
     *   <li>If an invite exists but is expired, it is renewed and marked as SENT</li>
     *   <li>If no invite exists, a new one is created and marked as SENT</li>
     * </ul>
     *
     * <p>Batch limitation:
     * If the number of unique emails exceeds the limit (MAX=500),
     * the method returns a response where all entries are FAILED with reason "batch_limit_exceeded".
     *
     * @param emails list of email addresses to invite
     * @return bulk invite result containing sent, skipped and failed entries
     */
    @Override
    public BulkInviteResponse bulkInvite(List<String> emails) {
    final int MAX = 500; 

    // defensiv: null / leer
    if (emails == null) {
        return new BulkInviteResponse(0, 0,
            List.of(), List.of(), List.of());
    }

    final int totalRequested = emails.size();

    // normalisieren (trim, toLowerCase, leere filtern, Duplikate)
    LinkedHashSet<String> unique = emails.stream()
        .filter(Objects::nonNull)
        .map(String::trim)
        .map(String::toLowerCase)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toCollection(LinkedHashSet::new));

    final int totalUnique = unique.size();

    // limit
    if (totalUnique > MAX) {
        // alles als failed zurückgeben 
        List<Result> failed = unique.stream()
            .map(e -> new Result(e, "FAILED", "batch_limit_exceeded"))
            .toList();
        return new BulkInviteResponse(totalRequested, totalUnique,
            List.of(), List.of(), failed);
    }

    List<Result> sent = new ArrayList<>();
    List<Result> skipped = new ArrayList<>();
    List<Result> failed = new ArrayList<>();

    for (String email : unique) {
        // Format prüfen
        try {
            assertHtwDomain(email); // wirf BAD_REQUEST bei Verstoß
        } catch (Exception ex) {
            failed.add(new Result(email, "FAILED", "invalid_email"));
            continue;
        }

        // bereits registriert?
        Optional<User> existingUser = userRepo.findByEmail(email);
        if (existingUser.isPresent()) {
            skipped.add(new Result(email, "SKIPPED", "already_registered"));
            continue;
        }

        // Invite vorhanden?
        var inviteOpt = inviteRepo.findByEmail(email);
        if (inviteOpt.isPresent()) {
            var inv = inviteOpt.get();
            if (!isExpired(inv.getInvitedAt()) && inv.getStatus() == InviteStatus.INVITED) {
                // aktive Einladung --> überspringen
                skipped.add(new Result(email, "SKIPPED", "already_invited_active"));
                continue;
            } else {
                // abgelaufen --> erneuern
                inv.setStatus(InviteStatus.INVITED);
                inv.setInvitedAt(Instant.now());
                inviteRepo.save(inv);
                sent.add(new Result(email, "SENT", "renewed"));
                continue;
            }
        }

        // ein Invite --> neu anlegen
        var created = new htw.wahlomat.wahlomat.model.admin.CandidateInvite(email);
        created.setStatus(InviteStatus.INVITED);
        created.setInvitedAt(Instant.now());
        inviteRepo.save(created);
        sent.add(new Result(email, "SENT", "created"));
    }

    return new BulkInviteResponse(totalRequested, totalUnique, sent, skipped, failed);
}
}
