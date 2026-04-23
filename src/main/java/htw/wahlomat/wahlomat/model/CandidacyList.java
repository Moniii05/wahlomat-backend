package htw.wahlomat.wahlomat.model;


import jakarta.persistence.*;
/**
 * Entity representing a candidate list within a specific committee.
 *
 * <p>A CandidacyList groups candidates that run together under the same
 * list name and list number for a specific committee.</p>
 *
 * <p>Each list belongs to exactly one committee (identified by committeeId).
 * The list number is used for ballot ordering.</p>
 */
@Entity
@Table(name = "lists")
public class CandidacyList
{
    /**
     * Primary key of the list (auto-generated surrogate key).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "list_id")
    private Long listId; // Surrogate Key

    /**
     * Official number of the list (e.g. 1, 2, 3).
     * Used for ballot ordering.
     */
    @Column(name = "number", nullable = false)
    private int number;

    /**
     * Name of the list (e.g. "Campusliste", "Liste Zukunft").
     */
    @Column(name = "list_name")
    private String listName;


    /**
     * Identifier of the committee this list belongs to.
     * References a committee defined in static data.
     */
    @Column(name = "committee_id")
    private String committeeId;

    /**
     * Default constructor required by JPA.
     */
    public CandidacyList() {}

    /**
     * Creates a new CandidacyList.
     *
     * @param number      official list number
     * @param listName    display name of the list
     * @param committeeId committee identifier
     */
    public CandidacyList(int number, String listName, String committeeId)
    {
        this.number = number;
        this.listName = listName;
        this.committeeId = committeeId;
    }

    /** @return list ID */
    public Long getListId() {
        return this.listId;
    }

    /** @return list number */
    public int getNumber() {
        return this.number;
    }

    /** @return list name */
    public String getListName() {
        return this.listName;
    }

    /** @return committee identifier */
    public String getCommitteeId() {
        return this.committeeId;
    }

    /**
     * Sets the list number.
     *
     * @param number new list number
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * Sets the list name.
     *
     * @param listName new list name
     */
    public void setListName(String listName) {
        this.listName = listName;
    }

    /**
     * Sets the committee identifier.
     *
     * @param committeeId new committee ID
     */
    public void setCommitteeId(String committeeId) {
        this.committeeId = committeeId;
    }
}