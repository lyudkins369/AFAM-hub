package it.unipa.afam.repository;

import it.unipa.afam.entity.Risorsa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RisorsaRepository extends JpaRepository<Risorsa, Long> {
    List<Risorsa> findByStudenteId(Long studenteId);
}
