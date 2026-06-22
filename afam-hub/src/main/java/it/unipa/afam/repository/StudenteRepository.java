package it.unipa.afam.repository;

import it.unipa.afam.entity.Studente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudenteRepository extends JpaRepository<Studente, Long> {
    List<Studente> findByNomeContainingIgnoreCaseAndCognomeContainingIgnoreCase(String nome, String cognome);
}
