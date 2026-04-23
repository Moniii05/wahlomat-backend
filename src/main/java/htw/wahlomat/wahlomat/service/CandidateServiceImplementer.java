package htw.wahlomat.wahlomat.service;

import htw.wahlomat.wahlomat.dto.CandidateProfileRequest;
import htw.wahlomat.wahlomat.dto.CandidateProfileResponse;
import htw.wahlomat.wahlomat.dto.CandidacyRequest;
import htw.wahlomat.wahlomat.dto.CandidacyResponse;


//entities aus profilePage
import htw.wahlomat.wahlomat.model.CandidacyList;
import htw.wahlomat.wahlomat.model.User;
import htw.wahlomat.wahlomat.model.profilePage.CandidateProfile;
import htw.wahlomat.wahlomat.model.profilePage.Candidacy;

//repositories aus profilePage
import htw.wahlomat.wahlomat.repository.CandidacyListRepository;
import htw.wahlomat.wahlomat.repository.UserRepository;
import htw.wahlomat.wahlomat.repository.profilePage.CandidateProfileRepository;
import htw.wahlomat.wahlomat.repository.profilePage.CandidacyRepository;

import htw.wahlomat.wahlomat.staticData.StaticData;
import htw.wahlomat.wahlomat.staticData.Committee;


//import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

// security für admin check
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;

import java.util.Objects;


/**
 * Concrete service implementation for candidate profile and candidacy management.
 *
 * <p>This service contains the business rules for:
 * <ul>
 *   <li>Reading and updating a candidate's profile</li>
 *   <li>Creating, updating and deleting candidacies</li>
 *   <li>Validating faculty/committee/list constraints using {@link StaticData}</li>
 * </ul>
 *
 * <p>Important rules implemented here:
 * <ul>
 *   <li>Profiles must exist (registration should have created them)</li>
 *   <li>Only one candidacy per user and committee is allowed</li>
 *   <li>Committee and list must be consistent and (if applicable) faculty-conform</li>
 *   <li>When the faculty changes, faculty-dependent candidacies are removed</li>
 * </ul>
 */
@Service
public class CandidateServiceImplementer implements CandidateService {

    private final CandidateProfileRepository profileRepo;
    private final CandidacyRepository candidacyRepo;
    private final UserRepository userRepo;
    private final CandidacyListRepository listRepo;
    private final StaticData staticData;


    public CandidateServiceImplementer(CandidateProfileRepository CandidateProfileRepo,
                                       CandidacyRepository candidacyRepo,
                                       UserRepository userRepo,
                                       CandidacyListRepository listRepo,
                                       StaticData staticData) {
        this.profileRepo = CandidateProfileRepo;
        this.candidacyRepo = candidacyRepo;
        this.userRepo = userRepo;
        this.listRepo = listRepo;
        this.staticData = staticData;
    }

    private boolean isFacultyDependentCommittee(String committeeId) {
        if (committeeId == null || committeeId.isBlank()) return false;

        // staticData (FACULTY != null => fachbereichsabhängig)
        return staticData.findCommitteeById(committeeId)
                .map(c -> c.FACULTY() != null)
                // falls committeeId nicht in staticData steht
                .orElseGet(() -> committeeId.startsWith("FSR") || committeeId.startsWith("FBR"));
    }

    /**
     * Deletes all candidacies of a user that are faculty-dependent.
     *
     * <p>This is used when the candidate changes their faculty,
     * so that old faculty-specific committees no longer remain selected.
     *
     * @param user the user whose faculty-dependent candidacies should be removed
     */
    private void deleteFacultyDependentCandidacies(User user) {
        List<Candidacy> all = candidacyRepo.findByUser(user);

        List<Candidacy> toDelete = all.stream()
                .filter(c -> isFacultyDependentCommittee(c.getCommitteeId()))
                .toList();

        if (!toDelete.isEmpty()) {
            candidacyRepo.deleteAll(toDelete);
        }
    }


    /**
     * Loads and returns the candidate profile for the given user.
     *
     * @param user the user whose profile should be loaded
     * @return the profile as {@link CandidateProfileResponse}
     * @throws ResponseStatusException with 404 if the profile does not exist
     */
    @Override
    @Transactional
    public CandidateProfileResponse getProfile(User user) {
        // holt profil, wenn keins existiert, leeres Default-Profil zurückgeben,
        // damit FE immer rendern kann
        // profilerepo erbt meth. von JPA
        return profileRepo.findByUser(user)
                // map= meth.(wenn wert drin-> dann wandelt um) von otional(container) = leer mögl.
                .map(this::toProfileResponse)
                // .orElseGet(() -> new CandidateProfileResponse("", "", 1L, ""));
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND, "Profil nicht gefunden" // sollte bei registrierung angelegt werden
                ));
    }


    /**
     * Updates an existing candidate profile.
     *
     * <p>Validation rules:
     * <ul>
     *   <li>firstname and lastname must not be null/blank</li>
     *   <li>user must exist</li>
     *   <li>profile must exist</li>
     * </ul>
     *
     * <p>If the faculty changes, faculty-dependent candidacies are removed.
     *
     * @param user the user whose profile should be updated
     * @param profileRequestDto new profile values
     * @throws ResponseStatusException with 400/404 if validation fails
     */
    @Override
    @Transactional
    public void saveProfile(User user, CandidateProfileRequest profileRequestDto) {
        // Validierung
        if (profileRequestDto.firstname() == null || profileRequestDto.firstname().isBlank())
            throw new ResponseStatusException(BAD_REQUEST, "Vorname fehlt");
        if (profileRequestDto.lastname() == null || profileRequestDto.lastname().isBlank())
            throw new ResponseStatusException(BAD_REQUEST, "Nachname fehlt");

        // User muss existieren
        User existingUser = userRepo.findById(user.getUserId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User nicht gefunden"));

        // profil muss existieren
        CandidateProfile entity = profileRepo.findByUser(existingUser)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND,
                        "Profil nicht gefunden"
                ));

        // faculty change erkennen
        Long oldFacultyId = entity.getFacultyId();
        Long newFacultyId = profileRequestDto.facultyId();
        boolean facultyChanged = !Objects.equals(oldFacultyId, newFacultyId);

        apply(entity, profileRequestDto);
        profileRepo.save(entity);

        // wenn FB geändert
        if (facultyChanged) {
            deleteFacultyDependentCandidacies(existingUser);
        }
    }

    // ------ Kandidaturen
    /**
     * Returns all candidacies of the given user.
     *
     * @param user the user whose candidacies should be returned
     * @return list of candidacies as {@link CandidacyResponse}
     */
    @Override
    @Transactional
    public List<CandidacyResponse> listCandidacies(User user) {
        // stream= folge elemente eg aus List, die man auf pipeline anwendet
        return candidacyRepo.findByUser(user).stream() // stream = startet SPI pipeline
                .map(this::toCandidacyResponse) // = jede entity -> DTO umwandlen
                .toList(); // fertige Liste erzeugen
    }

    /**
     * Returns all candidacies belonging to a specific candidacy list.
     *
     * @param candidacyList the list entity
     * @return list of candidacies as {@link CandidacyResponse}
     */
    @Override
    @Transactional
    public List<CandidacyResponse> getCandidaciesByCandidacyList(CandidacyList candidacyList) {
        return candidacyRepo.findByCandidacyList(candidacyList).stream()
                .map(this::toCandidacyResponse)
                .toList();
    }

    /**
     * Validates that:
     * <ul>
     *   <li>the committee exists in {@link StaticData}</li>
     *   <li>the user has a profile with facultyId</li>
     *   <li>the committee is faculty-compatible (if faculty-dependent)</li>
     *   <li>the list exists and belongs to the committee</li>
     * </ul>
     *
     * @param user the user creating/updating a candidacy
     * @param committeeId the selected committee id
     * @param listId the selected list id
     * @throws ResponseStatusException with 400 if validation fails
     */
    private void validateFacultyAndList(User user, String committeeId, Long listId) {

        // Committee muss existieren (StaticData)
        Committee committee = staticData.findCommitteeById(committeeId)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Gremium existiert nicht."));

        // Profil laden (facultyId)
        Long userFacultyId = profileRepo.findByUser(user)
                .map(CandidateProfile::getFacultyId)
                // .orElse(1L);
                .orElseThrow(() -> new ResponseStatusException(
                        BAD_REQUEST, "Profil/Fachbereich fehlt"
                ));

        // Fachbereichsregel: FACULTY == null ODER == facultyId
        Integer committeeFaculty = committee.FACULTY();
        if (committeeFaculty != null && committeeFaculty.longValue() != userFacultyId.longValue()) {
            throw new ResponseStatusException(BAD_REQUEST, "Gremium ist nicht fachbereichskonform.");
        }

        // Liste muss existieren
        CandidacyList list = listRepo.findById(listId)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Liste existiert nicht."));

        // Liste muss zum Committee gehören
        if (!committeeId.equals(list.getCommitteeId())) {
            throw new ResponseStatusException(BAD_REQUEST, "Liste gehört nicht zum gewählten Gremium.");
        }
    }

    /**
     * Creates a new candidacy for a user.
     *
     * <p>Rules:
     * <ul>
     *   <li>Only one candidacy per committee and user</li>
     *   <li>Committee/list/faculty constraints must be valid</li>
     * </ul>
     *
     * @param user the user creating the candidacy
     * @param candidacyRequestDto the request containing committeeId and listId
     * @return created candidacy as {@link CandidacyResponse}
     * @throws ResponseStatusException with 400/409 if rules are violated
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CandidacyResponse createCandidacy(User user, CandidacyRequest candidacyRequestDto) {
        // regel: pro Gremium nur 1 Kandidatur je User
        if (candidacyRepo.existsByUserAndCommitteeId(user, candidacyRequestDto.committeeId())) {
            throw new ResponseStatusException(CONFLICT, "Für dieses Gremium existiert bereits eine Kandidatur.");
        }
        validateFacultyAndList(user, candidacyRequestDto.committeeId(), candidacyRequestDto.listId());

        CandidacyList list = listRepo.findById(candidacyRequestDto.listId())
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Liste existiert nicht."));

        Candidacy saved = candidacyRepo.save(
                new Candidacy(user, candidacyRequestDto.committeeId(), this.listRepo.getById(candidacyRequestDto.listId()))
        );
        return toCandidacyResponse(saved);
        // to... hilfsklasse in service  ( wandelt Entity -> DTO)
    }

    /**
     * Updates an existing candidacy.
     *
     * <p>Rules:
     * <ul>
     *   <li>The committee must not be duplicated for the same user</li>
     *   <li>Committee/list/faculty constraints must be valid</li>
     * </ul>
     *
     * @param candidacyId the id of the candidacy to update
     * @param candidacyRequestDto new committee/list selection
     * @throws ResponseStatusException with 400/404/409 if rules are violated
     */
    @Override
    @Transactional
    public void updateCandidacy(Long candidacyId, CandidacyRequest candidacyRequestDto) {
        // Kandidatur muss zum User gehören (Admin darf jede bearbeiten)
        Candidacy existing = candidacyRepo.findByCandidacyId(candidacyId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Kandidatur nicht gefunden."));


        // Gremium nicht doppelt belegen (außer es ist dieselbe Kandidatur)
        boolean duplicateCommittee = candidacyRepo.findByUser(existing.getUser()).stream()
                .anyMatch(c -> !c.getCandidacyId().equals(candidacyId)
                        && c.getCommitteeId().equals(candidacyRequestDto.committeeId()));
        if (duplicateCommittee) {
            throw new ResponseStatusException(CONFLICT, "Dieses Gremium ist bei dir bereits belegt.");
        }

        validateFacultyAndList(existing.getUser(), candidacyRequestDto.committeeId(), candidacyRequestDto.listId());

        existing.setCommitteeId(candidacyRequestDto.committeeId());

        CandidacyList list = listRepo.findById(candidacyRequestDto.listId())
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Liste existiert nicht."));

        existing.setCandidacyList(list);
        candidacyRepo.save(existing);
    }

    /**
     * Deletes an existing candidacy.
     *
     * <p>This method uses {@code REQUIRES_NEW} propagation and explicitly flushes,
     * so the deletion is persisted immediately.
     *
     * @param candidacyId the id of the candidacy to delete
     * @throws ResponseStatusException with 404 if the candidacy does not exist
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteCandidacy(Long candidacyId) {
        Candidacy existing = candidacyRepo.findByCandidacyId(candidacyId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Kandidatur nicht gefunden."));
        candidacyRepo.delete(existing);
        candidacyRepo.flush(); //zwingt sofortiges schreiben in DB

        System.out.println("Kandidatur " + candidacyId + " wurde gelöscht und geflusht");
    }

    /**
     * Maps a {@link CandidateProfile} entity to its response DTO.
     *
     * @param e profile entity
     * @return {@link CandidateProfileResponse}
     */
    private CandidateProfileResponse toProfileResponse(CandidateProfile e) {
        return new CandidateProfileResponse(
                e.getFirstname(),
                e.getLastname(),
                e.getFacultyId(),
                e.getAboutMe()
        );
    }

    /**
     * Applies request values to the profile entity.
     *
     * @param e target entity
     * @param r request DTO
     */
    private void apply(CandidateProfile e, CandidateProfileRequest r) {
        e.setFirstname(r.firstname());
        e.setLastname(r.lastname());
        e.setFacultyId(r.facultyId());
        e.setAboutMe(r.aboutMe());
    }


    /**
     * Maps a {@link Candidacy} entity to its response DTO.
     *
     * @param e candidacy entity
     * @return {@link CandidacyResponse}
     */
    private CandidacyResponse toCandidacyResponse(Candidacy e) {
        return new CandidacyResponse(
                e.getCandidacyId(),
                e.getCommitteeId(),
                e.getCandidacyList().getListId()
        );
    }
}
