package it.unipa.afam.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "profilo")
public class Profilo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_utente")
    private String nomeUtente;

    @Column(name = "istituzione_afam")
    private String istituzioneAfam;

    @Column(name = "corso_studi")
    private String corsoStudi;

    @Column(name = "anno_accademico")
    private String annoAccademico;

    @Column(name = "anno_di_corso")
    private Integer annoDiCorso;

    @Column(name = "area_disciplinare")
    private String areaDisciplinare;

    @Column(name = "foto_profilo")
    private String fotoProfilo;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "foto_contenuto", columnDefinition = "LONGBLOB")
    private byte[] fotoContenuto;

    @Column(name = "foto_content_type")
    private String fotoContentType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studente_id", nullable = false, unique = true)
    private Studente studente;

    public Profilo() {}

    public Profilo(String nomeUtente, Studente studente) {
        this.nomeUtente = nomeUtente;
        this.studente = studente;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNomeUtente() { return nomeUtente; }
    public void setNomeUtente(String nomeUtente) { this.nomeUtente = nomeUtente; }
    public String getIstituzioneAfam() { return istituzioneAfam; }
    public void setIstituzioneAfam(String istituzioneAfam) { this.istituzioneAfam = istituzioneAfam; }
    public String getCorsoStudi() { return corsoStudi; }
    public void setCorsoStudi(String corsoStudi) { this.corsoStudi = corsoStudi; }
    public String getAnnoAccademico() { return annoAccademico; }
    public void setAnnoAccademico(String annoAccademico) { this.annoAccademico = annoAccademico; }
    public Integer getAnnoDiCorso() { return annoDiCorso; }
    public void setAnnoDiCorso(Integer annoDiCorso) { this.annoDiCorso = annoDiCorso; }
    public String getAreaDisciplinare() { return areaDisciplinare; }
    public void setAreaDisciplinare(String areaDisciplinare) { this.areaDisciplinare = areaDisciplinare; }
    public String getFotoProfilo() { return fotoProfilo; }
    public void setFotoProfilo(String fotoProfilo) { this.fotoProfilo = fotoProfilo; }
    public byte[] getFotoContenuto() { return fotoContenuto; }
    public void setFotoContenuto(byte[] fotoContenuto) { this.fotoContenuto = fotoContenuto; }
    public String getFotoContentType() { return fotoContentType; }
    public void setFotoContentType(String fotoContentType) { this.fotoContentType = fotoContentType; }
    public Studente getStudente() { return studente; }
    public void setStudente(Studente studente) { this.studente = studente; }
}
