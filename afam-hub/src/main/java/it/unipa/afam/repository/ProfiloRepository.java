package it.unipa.afam.repository;

import it.unipa.afam.entity.Profilo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProfiloRepository extends JpaRepository<Profilo, Long> {
    Optional<Profilo> findByStudenteId(Long studenteId);
}
