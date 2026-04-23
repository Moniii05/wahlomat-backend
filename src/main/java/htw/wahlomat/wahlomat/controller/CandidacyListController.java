package htw.wahlomat.wahlomat.controller;

import htw.wahlomat.wahlomat.dto.CandidacyListRequest;
import htw.wahlomat.wahlomat.dto.CandidacyListResponse;
import htw.wahlomat.wahlomat.model.CandidacyList;
import htw.wahlomat.wahlomat.service.CandidacyListService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for managing {@link CandidacyList} resources.
 *
 * <p>Provides endpoints to:</p>
 * <ul>
 *   <li>Retrieve all candidacy lists</li>
 *   <li>Retrieve lists filtered by committee</li>
 *   <li>Create new lists (ADMIN only)</li>
 *   <li>Delete existing lists (ADMIN only)</li>
 * </ul>
 *
 * <p>Base path: {@code /api/lists}</p>
 *
 * <p>Authorization rules:</p>
 * <ul>
 *   <li>GET endpoints are publicly accessible (used by voters)</li>
 *   <li>POST and DELETE endpoints require {@code ROLE_ADMIN}</li>
 * </ul>
 */

@RestController
@RequestMapping("/api/lists")
@Tag( name = "CandidacyList", description = "CandidacyList Management API")
public class CandidacyListController {
    private final CandidacyListService candidacyListService;

    /**
     * Creates a new controller instance.
     *
     * @param candidacyListService service layer handling business logic
     */
        public CandidacyListController(CandidacyListService candidacyListService) {
            this.candidacyListService = candidacyListService;
        }

    /**
     * Returns all available candidacy lists.
     *
     * <p>HTTP: {@code GET /api/lists}</p>
     *
     * @return list of {@link CandidacyListResponse} objects
     */

    @GetMapping
        public ResponseEntity<List<CandidacyListResponse>> getCandidacyLists() {
            List<CandidacyList> lists = candidacyListService.getAllCandidacyLists();
            List<CandidacyListResponse> candidacyListResponses = lists.stream()
                    .map(list -> new CandidacyListResponse(
                            list.getListId(),
                            list.getNumber(),
                            list.getListName(),
                            list.getCommitteeId()
                    ))
                    .toList();
            return ResponseEntity           // ResponseEntity wird erzeugt
                    .status(HttpStatus.OK)  // Statuscode wird auf OK gesetzt
                    .body(candidacyListResponses);       // body wird definiert
        }

    /**
     * Returns all candidacy lists belonging to a specific committee.
     *
     * <p>HTTP: {@code GET /api/lists/{committeeId}}</p>
     *
     * <p>If no lists exist for the given committee, an empty list is returned
     * with HTTP status {@code 200 OK}.</p>
     *
     * @param committeeId identifier of the committee
     * @return list of {@link CandidacyListResponse}
     */

    @GetMapping("/{committeeId}")
        public ResponseEntity<List<CandidacyListResponse>> getCandidacyListsByCommittee(@PathVariable String committeeId) {
            List<CandidacyList> lists = candidacyListService.findAllByCommitteeID(committeeId);

            // Überprüfen, ob die Liste leer ist
            if(lists.isEmpty())
            {
                return ResponseEntity.ok(new ArrayList<>());  //200 statt 404: leeres Ergebnis/ Anfrage erfolgreich statt Resource existiert nicht
            }
            else
            {  // Mapping der CandidacyList-Objekte zu CandidacyListResponse-Objekten
                List<CandidacyListResponse> candidacyListResponses = lists.stream()
                        .map(list -> new CandidacyListResponse(
                                list.getListId(),
                                list.getNumber(),
                                list.getListName(),
                                list.getCommitteeId()
                        ))
                        .toList();
                return ResponseEntity           // ResponseEntity wird erzeugt
                        .status(HttpStatus.OK)  // Statuscode wird auf OK gesetzt
                        .body(candidacyListResponses);       // body wird definiert
            }
        }


    /**
     * Creates a new candidacy list.
     *
     * <p>HTTP: {@code POST /api/lists}</p>
     *
     * <p>Access: {@code ROLE_ADMIN} only.</p>
     *
     * @param request request body containing list number, name and committee ID
     * @return created {@link CandidacyListResponse} with HTTP status {@code 201 Created}
     */

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CandidacyListResponse> createList(@Valid @RequestBody CandidacyListRequest request) {
        CandidacyList created = candidacyListService.createList(request);
        if (created != null) {
            CandidacyListResponse response = new CandidacyListResponse(
                    created.getListId(),
                    created.getNumber(),
                    created.getListName(),
                    created.getCommitteeId()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Deletes an existing candidacy list by its ID.
     *
     * <p>HTTP: {@code DELETE /api/lists/{listId}}</p>
     *
     * <p>Access: {@code ROLE_ADMIN} only.</p>
     *
     * @param listId ID of the list to delete
     * @return {@code 204 No Content} if deletion was successful
     */

    @DeleteMapping("/{listId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCandidacyListById(@PathVariable Long listId) {
        this.candidacyListService.deleteCandidacyListById(listId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

/*
    @GetMapping("/{userId}")
    public ResponseEntity<CandidacyListResponse> getListById(@PathVariable Long userId) {
        return candidacyListService.getListById(userId)
                .map(list -> ResponseEntity.ok(new CandidacyListResponse(
                        list.getListID(),
                        list.getNumber(),
                        list.getListName(),
                        list.getCommitteeID()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

     */

}
