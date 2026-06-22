package it.unipa.afam.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Rappresenta una raccolta di Risorse selezionate dallo Studente per essere condivise.
 * Invarianti: nome univoco per ciascuno Studente; un Preset pubblico è ricercabile dall'Esaminatore Esterno.
 */
@Entity
@Table(name = "preset")
public class Preset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private boolean pubblico = false;

    @Column(name = "data_creazione", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataCreazione;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studente_id", nullable = false)
    private Studente studente;

    // Associazione M-N con Risorsa (tabella PRESET_RISORSA)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "preset_risorsa",
        joinColumns = @JoinColumn(name = "preset_id"),
        inverseJoinColumns = @JoinColumn(name = "risorsa_id")
    )
    private List<Risorsa> risorse = new ArrayList<>();

    // Associazione 0-1 con Link
    @OneToOne(mappedBy = "preset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Link link;

    public Preset() {}

    public Preset(String nome, Studente studente) {
        this.nome = nome;
        this.studente = studente;
        this.dataCreazione = new Date();
    }

    /**
     * Imposta il flag pubblico/privato (ODD §5.1).
     */
    public void impostaPubblico(boolean flag) {
        this.pubblico = flag;
    }

    public void aggiungiRisorsa(Risorsa r) {
        if (!risorse.contains(r)) {
            risorse.add(r);
        }
    }

    public void rimuoviRisorsa(Risorsa r) {
        risorse.remove(r);
    }

    // Getter e Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public boolean isPubblico() { return pubblico; }
    public void setPubblico(boolean pubblico) { this.pubblico = pubblico; }

    public Date getDataCreazione() { return dataCreazione; }
    public void setDataCreazione(Date dataCreazione) { this.dataCreazione = dataCreazione; }

    public Studente getStudente() { return studente; }
    public void setStudente(Studente studente) { this.studente = studente; }

    public List<Risorsa> getRisorse() { return risorse; }
    public void setRisorse(List<Risorsa> risorse) { this.risorse = risorse; }

    public Link getLink() { return link; }
    public void setLink(Link link) { this.link = link; }
}
