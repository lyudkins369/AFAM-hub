package it.unipa.afam.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta una Sezione tematica (Categoria) creata dallo Studente
 * per organizzare le proprie risorse (es. "Audio", "Immagini", "Tesi").
 * Invarianti: nome non vuoto.
 */
@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studente_id", nullable = false)
    private Studente studente;

    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Sottocategoria> sottocategorie = new ArrayList<>();

    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    private List<Risorsa> risorse = new ArrayList<>();

    public Categoria() {}

    public Categoria(String nome, Studente studente) {
        this.nome = nome;
        this.studente = studente;
    }

    // Getter e Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Studente getStudente() { return studente; }
    public void setStudente(Studente studente) { this.studente = studente; }

    public List<Sottocategoria> getSottocategorie() { return sottocategorie; }
    public void setSottocategorie(List<Sottocategoria> sottocategorie) { this.sottocategorie = sottocategorie; }

    public List<Risorsa> getRisorse() { return risorse; }
    public void setRisorse(List<Risorsa> risorse) { this.risorse = risorse; }
}
