package it.unipa.afam.entity;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("IMMAGINE")
public class Immagine extends Risorsa {
    private String risoluzione;

    @Override
    public TipoRisorsa getTipo() { return TipoRisorsa.IMMAGINE; }

    public String getRisoluzione() { return risoluzione; }
    public void setRisoluzione(String risoluzione) { this.risoluzione = risoluzione; }
}
