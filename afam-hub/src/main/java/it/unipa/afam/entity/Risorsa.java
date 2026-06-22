package it.unipa.afam.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Contenuto digitale caricato dallo Studente. Classe base astratta (single-table inheritance).
 * Il file e' memorizzato nel database (BLOB) cosi' e' accessibile da qualsiasi dispositivo.
 */
@Entity
@Table(name = "risorsa")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING)
public abstract class Risorsa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descrizione;

    @Column(name = "percorso_file", nullable = false)
    private String percorsoFile;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "contenuto", columnDefinition = "LONGBLOB")
    private byte[] contenuto;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "data_caricamento", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataCaricamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studente_id", nullable = false)
    private Studente studente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sottocategoria_id")
    private Sottocategoria sottocategoria;

    @ManyToMany(mappedBy = "risorse", fetch = FetchType.LAZY)
    private List<Preset> preset = new ArrayList<>();

    public abstract TipoRisorsa getTipo();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public String getPercorsoFile() { return percorsoFile; }
    public void setPercorsoFile(String percorsoFile) { this.percorsoFile = percorsoFile; }

    public byte[] getContenuto() { return contenuto; }
    public void setContenuto(byte[] contenuto) { this.contenuto = contenuto; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Date getDataCaricamento() { return dataCaricamento; }
    public void setDataCaricamento(Date dataCaricamento) { this.dataCaricamento = dataCaricamento; }

    public Studente getStudente() { return studente; }
    public void setStudente(Studente studente) { this.studente = studente; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public Sottocategoria getSottocategoria() { return sottocategoria; }
    public void setSottocategoria(Sottocategoria sottocategoria) { this.sottocategoria = sottocategoria; }

    public List<Preset> getPreset() { return preset; }
    public void setPreset(List<Preset> preset) { this.preset = preset; }
}
