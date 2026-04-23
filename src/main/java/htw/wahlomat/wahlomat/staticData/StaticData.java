package htw.wahlomat.wahlomat.staticData;


import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component // Spring macht Instanz(Bean) dieser Klasse damit man wo anders "injizieren" kann
// = Spring steckt autom. fertige Obj. in Konstruktor anstatt manuell new
public class StaticData {
    //  Fachbereiche
    private static final List<Integer> FACULTY = List.of(1, 2, 3, 4, 5);

    //  Gremien + ob FB relevant ist
    private static final List<Committee> COMMITTEES = List.of(
            // FSR mit Fachbereichen
            new Committee("FSR1", "Fachschaftsrat", 1),
            new Committee("FSR2", "Fachschaftsrat", 2),
            new Committee("FSR3", "Fachschaftsrat", 3),
            new Committee("FSR4", "Fachschaftsrat", 4),
            new Committee("FSR5", "Fachschaftsrat",5),

            // FBR mit Fachbereichen
            new Committee("FBR1", "Fachbereichsrat", 1),
            new Committee("FBR2", "Fachbereichsrat", 2),
            new Committee("FBR3", "Fachbereichsrat", 3),
            new Committee("FBR4", "Fachbereichsrat", 4),
            new Committee("FBR5", "Fachbereichsrat", 5),

            // Gremien OHNE Fachbereich
            new Committee("AS", "Akademischer Senat", null),
            new Committee("STUPA", "Studierendenparlament", null),
            new Committee("KUR", "Kuratorium", null)

    );


    //helper - für Validierung
    public Optional<Committee> findCommitteeById(String committeeId) {
        return COMMITTEES.stream()
                .filter(c -> c.committeeId().equals(committeeId))
                .findFirst();
    }

    public boolean committeeExists(String committeeId) {
        return COMMITTEES.stream()
                .anyMatch(c -> c.committeeId().equals(committeeId));
    }

    // für Dropdown "Fachbereich"
    public List<Integer> faculties() { return FACULTY; }
    // für Dropdown "Gremium"
    public List<Committee> committees() { return COMMITTEES; }


}
