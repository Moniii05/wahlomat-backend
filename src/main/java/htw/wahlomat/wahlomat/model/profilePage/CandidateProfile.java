package htw.wahlomat.wahlomat.model.profilePage;

import htw.wahlomat.wahlomat.model.User;
import jakarta.persistence.*;

@Entity 
@Table(name = "candidate_profiles") // pro User genau ein Profil
public class CandidateProfile {

   // primärschlüssel
  @Id
  @Column(name = "user_id")
  private Long id;   //speichert die id als Zahl


  // SPALTEN
  // FK auf user = PK in dieser Tabelle
  //@Id
  @OneToOne
  //@PrimaryKeyJoinColumn(name = "user_id", referencedColumnName = "user_id")
  @MapsId // ID = user_id vom User   Sagt: "Benutze die ID vom User-Objekt"
  @JoinColumn(name = "user_id")
  private User user; //Speichert die User-Referenz

  //profildaten 
  @Column(nullable = false) 
  private String firstname;

  @Column(nullable = false) 
  private String lastname;

  @Column(name = "faculty_id", nullable = false) 
  private Long facultyId;

  @Column(name = "about_me", columnDefinition = "text") 
  private String aboutMe;

  // wird von JPA gefordert (leer)
  public CandidateProfile() {} 


  public CandidateProfile(User user, String firstname, String lastname, Long facultyId, String aboutMe) {
    this.user = user; // ← @MapsId kümmert sich um id automatisch!
    this.firstname = firstname; 
    this.lastname = lastname; 
    this.facultyId = facultyId; 
    this.aboutMe = aboutMe;
  }

  // getter/setter damit JPA Felder lesen/schreiben kann 

    public Long getId(){
    return id; 
    // kein set weil kommt von DB generiert
  }

  public void setId(Long id){this.id = id; } // Braucht man für @MapsId!

  public User getUser(){
    return user;
  }

  public void setUser(User user) {  this.user = user; }

  public String getFirstname(){ 
    return firstname; 
  }

  public void setFirstname(String firstname){ 
    this.firstname = firstname; 
  }

  public String getLastname(){ 
   return lastname; 
  }

  public void setLastname(String lastname){ 
    this.lastname = lastname; 
  }

  public Long getFacultyId(){ 
    return facultyId; 
  }

  public void setFacultyId(Long facultyId){ 
    this.facultyId = facultyId; 
  }

  public String getAboutMe(){ 
    return aboutMe; 
  }

  public void setAboutMe(String aboutMe){ 
    this.aboutMe = aboutMe; 
  }




}

