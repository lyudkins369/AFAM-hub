package it.unipa.afam.entity;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;

/**
 * Rappresenta un collegamento condivisibile associato a un Preset.
 * Invarianti: url univoco; se statoApertura = true allora dataApertura non è nulla.
 */
@Entity
@Table(name = "link")
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String url;

    @Column(name = "nome_link")
    private String nomeLink;

    @Column(name = "stato_apertura", nullable = false)
    private boolean statoApertura = false;

    @Column(name = "data_apertura")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataApertura;

    @Column(name = "data_creazione", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataCreazione;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preset_id", nullable = false, unique = true)
    private Preset preset;

    public Link() {}

    public Link(Preset preset) {
        this.preset = preset;
        this.url = UUID.randomUUID().toString().substring(0, 8);
        this.dataCreazione = new Date();
    }

    /**
     * Registra l'apertura del link (ODD §3.1).
     */
    public void registraApertura(Date data) {
        this.statoApertura = true;
        this.dataApertura = data;
    }

    public boolean isAperto() {
        return statoApertura;
    }

    // Getter e Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getNomeLink() { return nomeLink; }
    public void setNomeLink(String nomeLink) { this.nomeLink = nomeLink; }

    public boolean isStatoApertura() { return statoApertura; }
    public void setStatoApertura(boolean statoApertura) { this.statoApertura = statoApertura; }

    public Date getDataApertura() { return dataApertura; }
    public void setDataApertura(Date dataApertura) { this.dataApertura = dataApertura; }

    public Date getDataCreazione() { return dataCreazione; }
    public void setDataCreazione(Date dataCreazione) { this.dataCreazione = dataCreazione; }

    public Preset getPreset() { return preset; }
    public void setPreset(Preset preset) { this.preset = preset; }
}
