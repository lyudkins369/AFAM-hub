package it.unipa.afam.entity;

import jakarta.persistence.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Rappresenta le credenziali di accesso di uno Studente.
 * Composto da e-mail istituzionale e password (hash).
 * Invarianti: emailIstituzionale univoca; passwordHash sempre valorizzato.
 */
@Entity
@Table(name = "account")
public class Account {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email_istituzionale", nullable = false, unique = true)
    private String emailIstituzionale;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studente_id", nullable = false, unique = true)
    private Studente studente;

    public Account() {}

    public Account(String emailIstituzionale, String password, Studente studente) {
        this.emailIstituzionale = emailIstituzionale;
        this.passwordHash = encoder.encode(password);
        this.studente = studente;
    }

    /**
     * Verifica la password (ODD §4.1.2).
     * Restituisce true se hash(pwd) corrisponde a passwordHash.
     */
    public boolean verificaPassword(String pwd) {
        return encoder.matches(pwd, this.passwordHash);
    }

    /**
     * Aggiorna la password (ODD §4.1.2).
     */
    public void aggiornaPassword(String nuova) {
        this.passwordHash = encoder.encode(nuova);
    }

    // Getter e Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmailIstituzionale() { return emailIstituzionale; }
    public void setEmailIstituzionale(String emailIstituzionale) { this.emailIstituzionale = emailIstituzionale; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Studente getStudente() { return studente; }
    public void setStudente(Studente studente) { this.studente = studente; }
}
