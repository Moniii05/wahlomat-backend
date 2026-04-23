package htw.wahlomat.wahlomat.service;

import htw.wahlomat.wahlomat.dto.CandidateProfileRequest;
import htw.wahlomat.wahlomat.dto.CandidateProfileResponse;
import htw.wahlomat.wahlomat.dto.CandidacyRequest;
import htw.wahlomat.wahlomat.dto.CandidacyResponse;
import htw.wahlomat.wahlomat.model.CandidacyList;
import htw.wahlomat.wahlomat.model.User;

import java.util.List;

/**
 * Service interface for managing candidate profiles and candidacies.
 *
 * <p>This interface defines the contract between the controller layer and
 * the business logic layer. Controllers interact only with this abstraction,
 * not with persistence or implementation details.
 *
 * <p>Responsibilities:
 * <ul>
 *     <li>Reading and updating candidate profiles</li>
 *     <li>Managing candidacies (create, update, delete)</li>
 *     <li>Retrieving candidacies by user or list</li>
 * </ul>
 */
public interface CandidateService {
    /**
     * Returns the candidate profile for a given user.
     *
     * @param user the user whose profile should be retrieved
     * @return profile data as {@link CandidateProfileResponse}
     */
    CandidateProfileResponse getProfile(User user);

    /**
     * Saves or updates the candidate profile of a user.
     *
     * <p>Business logic may define whether this operation
     * creates a new profile or overwrites an existing one.
     *
     * @param user the user whose profile should be updated
     * @param profileRequestDto request data containing profile information
     */
    void saveProfile(User user, CandidateProfileRequest profileRequestDto);


    /**
     * Returns all candidacies of a specific user.
     *
     * <p>A candidacy represents a user's participation
     * in a specific committee and list.
     *
     * @param user the user whose candidacies should be retrieved
     * @return list of {@link CandidacyResponse}
     */
    List<CandidacyResponse> listCandidacies(User user);

    /**
     * Returns all candidacies that belong to a specific list.
     *
     * @param candidacyList the list whose candidacies should be retrieved
     * @return list of {@link CandidacyResponse}
     */
    List<CandidacyResponse> getCandidaciesByCandidacyList(CandidacyList candidacyList);


    /**
     * Creates a new candidacy for a given user.
     *
     * <p>This links a user to a committee and a specific list.
     *
     * @param user the user creating the candidacy
     * @param candidacyRequestDto request data containing committee and list information
     * @return created candidacy as {@link CandidacyResponse}
     */
    CandidacyResponse createCandidacy(User user, CandidacyRequest candidacyRequestDto);

    /**
     * Updates an existing candidacy.
     *
     * @param candidacyId ID of the candidacy to update
     * @param candidacyRequestDto request data containing updated values
     */
    void updateCandidacy(Long candidacyId, CandidacyRequest candidacyRequestDto);

    /**
     * Deletes an existing candidacy.
     *
     * @param candidacyId ID of the candidacy to delete
     */
    void deleteCandidacy(Long candidacyId);
}
