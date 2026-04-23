package htw.wahlomat.wahlomat.model.profilePage;

import htw.wahlomat.wahlomat.model.CandidacyList;
import htw.wahlomat.wahlomat.model.User;
import jakarta.persistence.*;

// kandidatur eines USers mit gewählter liste
@Entity
@Table(
  name = "candidacies",
  // unique macht regel: user darf je gremium nur eine Candidacy haben 
  uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","committee_id"})
)

public class Candidacy {
  @Id 
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "candidacy_id")
  private Long candidacyId;

  // referenz auf den Nutzer, der kandidiert
  // = foreign key auf user.userId
  // = "diese Kandidatur gehört zu diesem user"
  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // schlüssel vom gremium 
  @Column(name = "committee_id", nullable = false)
  private String committeeId;

  //referenz auf gewählte Liste innerhalb vom Gremium
  @ManyToOne
  @JoinColumn(name = "list_id", nullable = false)
  private CandidacyList candidacyList;

  // für JPA, sonst lädt nicht (kann Klasse nicht instanziieren)
  public Candidacy() {}

  public Candidacy(User user, String committeeId, CandidacyList candidacyList) {
    this.user = user;
    this.committeeId = committeeId;
    this.candidacyList = candidacyList;
  }

  public Long getCandidacyId(){return candidacyId;}
  // kein set nötig wegen generated von DB 

  public User getUser(){return user;}
 
  public String getCommitteeId(){return committeeId;}
  public void setCommitteeId(String committeeId) { this.committeeId = committeeId; } //eg FSR

  public CandidacyList getCandidacyList(){ return candidacyList; }
  public void setCandidacyList(CandidacyList candidacyList) {this.candidacyList = candidacyList;} //eg 10
}
