package htw.wahlomat.wahlomat.dto;
/**
 * Response DTO representing a faculty (department).
 *
 * @param id faculty identifier
 * @param name faculty display name
 */
public record FacultyResponse(
    Long id,
    String name
) {}
