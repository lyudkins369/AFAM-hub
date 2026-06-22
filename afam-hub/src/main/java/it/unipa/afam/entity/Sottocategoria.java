package it.unipa.afam.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta una Cartella (Sottocategoria) creata all'interno di una Categoria.
 * Invarianti: nome non vuoto; appartiene a esattamente una Categoria.
 */
@Entity
@Table(name = "sottocategoria")
public class Sottocategoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @OneToMany(mappedBy = "sottocategoria", fetch = FetchType.LAZY)
    private List<Risorsa> risorse = new ArrayList<>();

    public Sottocategoria() {}

    public Sottocategoria(String nome, Categoria categoria) {
        this.nome = nome;
        this.categoria = categoria;
    }

    // Getter e Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public List<Risorsa> getRisorse() { return risorse; }
    public void setRisorse(List<Risorsa> risorse) { this.risorse = risorse; }
}
