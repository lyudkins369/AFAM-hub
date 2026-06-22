package it.unipa.afam.entity;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("VIDEO")
public class Video extends Risorsa {
    private Integer durata;

    @Override
    public TipoRisorsa getTipo() { return TipoRisorsa.VIDEO; }

    public Integer getDurata() { return durata; }
    public void setDurata(Integer durata) { this.durata = durata; }
}
