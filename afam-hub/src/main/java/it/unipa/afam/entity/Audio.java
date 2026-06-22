package it.unipa.afam.entity;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("AUDIO")
public class Audio extends Risorsa {
    private Integer durata;

    @Override
    public TipoRisorsa getTipo() { return TipoRisorsa.AUDIO; }

    public Integer getDurata() { return durata; }
    public void setDurata(Integer durata) { this.durata = durata; }
}
