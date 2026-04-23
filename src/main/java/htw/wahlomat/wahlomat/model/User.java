// User Entity Klasse (Kandidaten und Admins)

package htw.wahlomat.wahlomat.model;

import java.util.Objects;
import jakarta.persistence.*;

/**
 * Entity representing a system user.
 *
 * <p>A user can have one of two roles:
 * <ul>
 *     <li>ADMIN – manages the system</li>
 *     <li>CANDIDATE – participates in elections</li>
 * </ul>
 *
 * <p>Email addresses are unique and serve as the logical identity
 * of the user within the application.</p>
 *
 * <p>Passwords are stored in hashed form (BCrypt).</p>
 */
@Entity
@Table(name="users")
public class User {

    /**
     * Primary key (surrogate key).
     */
    @Id
    @GeneratedValue // automatische Generierung der IDs
    @Column(name = "user_id")
    private Long userId;

    /**
     * Unique email address of the user.
     * Used for login authentication.
     */
    @Column(nullable = false, unique = true) // E-Mail darf nicht null sein und muss einzigartig sein (keine Duplikate)
    private String email;

    /**
     * Encrypted password (BCrypt hash).
     */
    @Column
    private String password;

    /**
     * Role of the user (ADMIN or CANDIDATE).
     */
    @Enumerated(EnumType.STRING) // speichert Enum als String in der DB
    @Column(nullable = false)
    private Role role; // verwendet Role Enum (s. Models)

    /**
     * Default constructor required by JPA.
     */
    public User() {} // parameterloser Konstruktor, Springboot Vorraussetzung

    /**
     * Creates a new user with password.
     * Typically used for admin creation.
     *
     * @param password encrypted password
     * @param email unique email address
     * @param role user role
     */
    public User(String password, String email, Role role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    /**
     * Creates a new user without password.
     * Used when creating invited candidates.
     *
     * @param email unique email address
     * @param role user role
     */
    public User( String email, Role role) {
        this.email = email;
        this.role = role;
    }

// GETTER
    /** @return database ID of the user */
    public Long getUserId() {return this.userId;}
    /** @return encrypted password */
    public String getPassword() {
        return this.password;
    }
    /** @return user email */
    public String getEmail() {
        return this.email;
    }
    /** @return role of the user */
    public Role getRole() {
        return this.role;
    }

//SETTER
    /** @param password encrypted password */
    public void setPassword(String password) {
        this.password = password;
    }

    /** @param email unique email address */
    public void setEmail(String email) {
        this.email = email;
    }
    /** @param role user role */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Users are considered equal if they share
     * the same email and role.
     */
    @Override
    public boolean equals(Object o) {
        if(o == null) return false;
        if (this == o) return true;
        if (!(o instanceof User))  return false;
        User user = (User) o;
        return
                Objects.equals(this.email, user.email) &&
                Objects.equals(this.role, user.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.email, this.role);
    }

    /**
     * Returns a safe string representation
     * without exposing the password.
     */
    @Override
    public String toString() {
        return "User{" +
                "email='" + this.email + '\'' + ", role='" + this.role + '\'' + '}';
    }
}