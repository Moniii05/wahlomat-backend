package htw.wahlomat.wahlomat.controller;


import htw.wahlomat.wahlomat.dto.FacultyResponse;
import htw.wahlomat.wahlomat.staticData.StaticData;
import htw.wahlomat.wahlomat.staticData.Committee;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * REST controller providing static lookup data for the application.
 *
 * <p>This controller exposes read-only endpoints for
 * retrieving static university structure data such as:</p>
 * <ul>
 *   <li>Faculties</li>
 *   <li>Committees</li>
 * </ul>
 *
 * <p>The data is retrieved from {@link StaticData} and
 * does not originate from the database.</p>
 *
 * <p>Base path: {@code /api/lookups}</p>
 *
 * <p>All endpoints are publicly accessible.</p>
 */

@RestController
@RequestMapping("/api/lookups") // Endpunkte nur für statische Lookups
@Tag( name = "staticData", description = "university structure API")
public class LookupController {

    private final StaticData staticData;

    /**
     * Creates a new lookup controller.
     *
     * @param staticData provider of static university data
     */

    public LookupController(StaticData staticData) {
        this.staticData = staticData;
    }


    /**
     * Returns all available faculties.
     *
     * <p>HTTP: {@code GET /api/lookups/faculties}</p>
     *
     * @return list of {@link FacultyResponse} objects
     */

    @GetMapping("/faculties")
    public List<FacultyResponse> faculties() {
        return staticData.faculties().stream()
                .map(id -> new FacultyResponse(id.longValue(), "Fachbereich " + id))
                .toList();
    }


    /**
     * Returns all available committees.
     *
     * <p>HTTP: {@code GET /api/lookups/committees}</p>
     *
     * @return list of {@link Committee} records
     */

    @GetMapping("/committees")
    public List<Committee> committees() {
        return staticData.committees().stream()
                //.map(this::toDto)
                .toList();
    }

}   



