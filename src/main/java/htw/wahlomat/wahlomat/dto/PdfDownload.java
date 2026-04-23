package htw.wahlomat.wahlomat.dto;
/**
 * DTO used for PDF export results.
 *
 * <p>Represents a single candidate entry inside the generated voter result PDF.</p>
 */
public class PdfDownload {

    private String firstName;
    private String lastName;
    private Integer listNumber;
    private String listName;
    private String aboutMe;
    private Integer matchingPercentage;
    /**
     * Creates an empty PDF download entry.
     */
    public PdfDownload() {
    }
    /**
     * Creates a PDF download entry with all candidate fields.
     *
     * @param firstname candidate first name
     * @param lastname candidate last name
     * @param listNumber number of the candidacy list
     * @param listName name of the candidacy list
     * @param aboutMe candidate description text
     * @param matchingPercentage match percentage (0..100)
     */
    public PdfDownload(
        String firstname,
        String lastname,
        Integer listNumber,
        String listName,
        String aboutMe,
        Integer matchingPercentage) {
        this.firstName = firstname;
        this.lastName = lastname;
        this.listNumber = listNumber;
        this.listName = listName;
        this.aboutMe = aboutMe;
        this.matchingPercentage = matchingPercentage;
    }
    /** @return candidate first name */
    public String getFirstName() { 
        return firstName; 
    }
    /** @param firstName candidate first name */
    public void setFirstName(String firstName) { 
        this.firstName = firstName; 
    }
    /** @return candidate last name */
    public String getLastName() { 
        return lastName; 
    }
    /** @param lastName candidate last name */
    public void setLastName(String lastName) { 
        this.lastName = lastName; 
    }
    /** @return number of the candidacy list */
    public Integer getListNumber() { 
        return listNumber; 
    }

    public void setListNumber(Integer listNumber) { 
        this.listNumber = listNumber; 
    }
    /** @return name of the candidacy list */
    public String getListName() { 
        return listName; 
    }

    /** @param listName name of the candidacy list */
    public void setListName(String listName) { 
        this.listName = listName; 
    }
    /** @return candidate description text */
    public String getAboutMe() { 
        return aboutMe; 
    }
    /** @param aboutMe candidate description text */
    public void setAboutMe(String aboutMe) { 
        this.aboutMe = aboutMe; 
    }
    /** @return match percentage (0..100) */
    public Integer getMatchingPercentage() { 
        return matchingPercentage; 
    }
    /** @param matchingPercentage match percentage (0..100) */
    public void setMatchingPercentage(Integer matchingPercentage) {
        this.matchingPercentage = matchingPercentage;
    }
}
