package it.unipa.afam.entity;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("DOCUMENTO")
public class Documento extends Risorsa {
    private String formato;

    @Override
    public TipoRisorsa getTipo() { return TipoRisorsa.DOCUMENTO; }

    public String getFormato() { return formato; }
    public void setFormato(String formato) { this.formato = formato; }
}
