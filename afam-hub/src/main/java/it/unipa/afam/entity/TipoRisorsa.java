package it.unipa.afam.entity;

/**
 * Enumerazione dei tipi di risorsa (discriminante per single-table inheritance).
 */
public enum TipoRisorsa {
    IMMAGINE,
    VIDEO,
    AUDIO,
    DOCUMENTO
}
