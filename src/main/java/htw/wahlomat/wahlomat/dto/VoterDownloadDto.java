package htw.wahlomat.wahlomat.dto;

import java.util.List;
/**
 * DTO containing all data required to render the voter result PDF.
 *
 * <p>Used as request body for the PDF export endpoint.</p>
 */
public class VoterDownloadDto {

    private String facultyLabel;
    private String committeeLabel;
    private String generatedAt;     
    private List<PdfDownload> candidates;
    /** Creates an empty voter download DTO. */
    public VoterDownloadDto() {
    }
    /** @return faculty label shown in the PDF */
    public String getFacultyLabel() { 
        return facultyLabel; 
    }

    /** @param facultyLabel faculty label shown in the PDF */
    public void setFacultyLabel(String facultyLabel) { 
        this.facultyLabel = facultyLabel; 
    }

    /** @return committee label shown in the PDF */
    public String getCommitteeLabel() { 
        return committeeLabel; 
    }

    /** @param committeeLabel committee label shown in the PDF */
    public void setCommitteeLabel(String committeeLabel) { 
        this.committeeLabel = committeeLabel; 
    }

    /** @return formatted generation timestamp */
    public String getGeneratedAt() { 
        return generatedAt; 
    }

    /** @param generatedAt formatted generation timestamp */
    public void setGeneratedAt(String generatedAt) { 
        this.generatedAt = generatedAt; 
    }

    /** @return list of candidate result entries */
    public List<PdfDownload> getCandidates() { 
        return candidates; 
    }

    /** @param candidates list of candidate result entries */
    public void setCandidates(List<PdfDownload> candidates) {
        this.candidates = candidates;
    }
}
