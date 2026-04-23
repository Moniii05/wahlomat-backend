package htw.wahlomat.wahlomat.controller;


import htw.wahlomat.wahlomat.dto.VoterDownloadDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Controller responsible for generating PDF export views.
 *
 * <p>This controller receives voter matching results from the frontend
 * and forwards them to a server-side HTML template for PDF rendering.</p>
 *
 * <p>Unlike REST controllers, this controller returns a view name
 * instead of JSON data.</p>
 *
 * <p>Base path: {@code /export}</p>
 *
 * <p>The returned view is rendered using a template engine
 * (e.g., Thymeleaf) and can be converted into a PDF.</p>
 */

@Controller
@RequestMapping("/export")
@CrossOrigin(origins = "http://localhost:4200")
public class PdfExportController {


    //private final ObjectMapper objectMapper;

    //public PdfExportController(ObjectMapper objectMapper) {
    //    this.objectMapper = objectMapper;
    //}

    /**
     * Generates a PDF export view for voter matching results.
     *
     * <p>HTTP: {@code POST /export/result}</p>
     *
     * <p>The request body contains all necessary data for rendering
     * the result document (faculty, committee, timestamp and candidate results).</p>
     *
     * <p>The data is added to the Spring {@link Model}
     * and forwarded to the {@code voter-result.html} template.</p>
     *
     * @param voterExportData DTO containing metadata and matching results
     * @param model Spring model used for template rendering
     * @return the name of the HTML template ("voter-result")
     */

    @PostMapping("/result")
    public String exportResult(@RequestBody VoterDownloadDto voterExportData, Model model) {

        // Labels + Meta daten
        model.addAttribute("facultyLabel", voterExportData.getFacultyLabel());
        model.addAttribute("committeeLabel", voterExportData.getCommitteeLabel());
        model.addAttribute("generatedAt", voterExportData.getGeneratedAt());

        // Kandidaten liste
        model.addAttribute("results", voterExportData.getCandidates());

        // (src/main/resources/templates/voter-result.html)
        return "voter-result";
    }








   /* 
     //http://localhost:8080/export/demo
    @GetMapping("/demo")
    public String exportDemo(Model model) {

        // 1) Meta-Infos fürs Template
        String facultyLabel = "Fachbereich 2";
        String committeeLabel = "Studierendenparlament";

        String generatedAt = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm"));

        // 2) Demo-Kandidaten (hier kannst du später das echte Matching-Ergebnis einsetzen)
        List<PdfDownload> results = List.of(
                new PdfDownload(
                        "Anna",
                        "Schneider",
                        3,
                        "Campusliste",
                        39,
                        "Noch eine Änderung von Hannah"
                ),
                new PdfDownload(
                        "Max",
                        "Mustermann",
                        1,
                        "Liste Zukunft",
                        28,
                        "Engagiert im Fachschaftsrat seit 2022."
                ),
                new PdfDownload(
                    "Lina",
                    "Müller",
                    1,
                    "Liste Zukunft",
                    28,
                    "Engagiert im Fachschaftsrat seit 2022."
            ),
            new PdfDownload(
                "Molly",
                "Sternchen",
                1,
                "Liste Zukunft",
                28,
                "Engagiert im Fachschaftsrat seit 2022."
        )
        );

        // 3) Daten ins Model legen – Namen müssen zum Thymeleaf-Template passen
        model.addAttribute("facultyLabel", facultyLabel);
        model.addAttribute("committeeLabel", committeeLabel);
        model.addAttribute("generatedAt", generatedAt);
        model.addAttribute("results", results);

        // 4) Name der HTML-Datei ohne .html
        return "voter-result";
    }  */
}
