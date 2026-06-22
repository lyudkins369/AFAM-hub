package it.unipa.afam.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta lo Studente registrato al sistema.
 * Possiede informazioni anagrafiche e di contatto.
 * Ogni Studente possiede esattamente un Account e un Profilo.
 */
@Entity
@Table(name = "studente")
public class Studente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String cognome;

    @Column(name = "codice_fiscale", nullable = false, unique = true)
    private String codiceFiscale;

    @Column(name = "email_personale")
    private String emailPersonale;

    @Column(nullable = false)
    private String cellulare;

    // Associazione 1-1 con Account
    @OneToOne(mappedBy = "studente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Account account;

    // Associazione 1-1 con Profilo
    @OneToOne(mappedBy = "studente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Profilo profilo;

    // Associazione 1-N con Categoria
    @OneToMany(mappedBy = "studente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Categoria> categorie = new ArrayList<>();

    // Associazione 1-N con Risorsa
    @OneToMany(mappedBy = "studente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Risorsa> risorse = new ArrayList<>();

    // Associazione 1-N con Preset
    @OneToMany(mappedBy = "studente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Preset> preset = new ArrayList<>();

    public Studente() {}

    public Studente(String nome, String cognome, String codiceFiscale, String emailPersonale, String cellulare) {
        this.nome = nome;
        this.cognome = cognome;
        this.codiceFiscale = codiceFiscale;
        this.emailPersonale = emailPersonale;
        this.cellulare = cellulare;
    }

    /**
     * Verifica se esiste un preset con il nome dato (ODD §3.1).
     */
    public boolean esistePresetConNome(String nome) {
        return preset.stream().anyMatch(p -> p.getNome().equalsIgnoreCase(nome));
    }

    // Getter e Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }

    public String getCodiceFiscale() { return codiceFiscale; }
    public void setCodiceFiscale(String codiceFiscale) { this.codiceFiscale = codiceFiscale; }

    public String getEmailPersonale() { return emailPersonale; }
    public void setEmailPersonale(String emailPersonale) { this.emailPersonale = emailPersonale; }

    public String getCellulare() { return cellulare; }
    public void setCellulare(String cellulare) { this.cellulare = cellulare; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public Profilo getProfilo() { return profilo; }
    public void setProfilo(Profilo profilo) { this.profilo = profilo; }

    public List<Categoria> getCategorie() { return categorie; }
    public void setCategorie(List<Categoria> categorie) { this.categorie = categorie; }

    public List<Risorsa> getRisorse() { return risorse; }
    public void setRisorse(List<Risorsa> risorse) { this.risorse = risorse; }

    public List<Preset> getPreset() { return preset; }
    public void setPreset(List<Preset> preset) { this.preset = preset; }
}
