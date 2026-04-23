package htw.wahlomat.wahlomat.model.admin;

import jakarta.persistence.*;
import java.time.Instant;
import htw.wahlomat.wahlomat.model.User;
/**
 * Entity representing an invitation for a candidate to register.
 *
 * <p>This table stores email-based invitations sent by an admin.
 * Each email can only have one active invite (unique constraint).</p>
 *
 * <p>Lifecycle:</p>
 * <ul>
 *   <li>Status = INVITED → invitation sent</li>
 *   <li>Status may change when user registers</li>
 *   <li>registeredUser is set after successful registration</li>
 * </ul>
 *
 * <p>The {@code invitedAt} timestamp is used for expiration logic (TTL),
 * e.g. invitation valid for 3 months.</p>
 */
@Entity
// verhindert doppel einträge 
@Table(name = "candidate_invites", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class CandidateInvite {
    /**
     * Primary key (auto-generated).
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Email address of the invited candidate.
     * Stored in lowercase.
     * Must be unique.
     */
    @Column(nullable = false, unique = true)
    private String email; // immer lowercase speichern
    /**
     * Current status of the invitation.
     */
    @Enumerated(EnumType.STRING)
        /** Timestamp when the invitation was created.
            * Used for expiration logic (TTL).
            */
    @Column(nullable = false)
    private InviteStatus status = InviteStatus.INVITED;

    @Column(nullable = false)
    private Instant invitedAt = Instant.now(); // für TTL
    /**
     * Reference to the registered user after successful signup.
     * Nullable until registration is completed.
     */
    @ManyToOne(optional = true)
    @JoinColumn(name = "registered_user_id")
    private User registeredUser;


    /**
     * Protected default constructor required by JPA.
     */
    protected CandidateInvite()
    /**
     * Creates a new candidate invite with the given email.
     *
     * @param emailLc lowercase email address
     */{}
    public CandidateInvite(String emailLc) {
        this.email = emailLc;
       
    }

    /** @return invite ID */
    public Long getId() { return id; }

    /** @return invited email address */
    public String getEmail() { return email; }

    /** @param email email address (lowercase) */
    public void setEmail(String email) { this.email = email; }

    /** @return invitation status */
    public InviteStatus getStatus() { return status; }

    /** @param status new invitation status */
    public void setStatus(InviteStatus status) { this.status = status; }

    /** @return invitation timestamp */
    public Instant getInvitedAt() { return invitedAt; }

    /** @param invitedAt invitation timestamp */
    public void setInvitedAt(Instant invitedAt) { this.invitedAt = invitedAt; }

    /** @return registered user (nullable) */
    public User getRegisteredUser() {
        return registeredUser;
    }

    /** @param registeredUser linked registered user */
    public void setRegisteredUser(User registeredUser) {
        this.registeredUser = registeredUser;
    }
}