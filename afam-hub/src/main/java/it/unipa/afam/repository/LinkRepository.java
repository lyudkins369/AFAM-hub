package it.unipa.afam.repository;

import it.unipa.afam.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LinkRepository extends JpaRepository<Link, Long> {
    Optional<Link> findByUrl(String url);
    List<Link> findByPresetStudenteId(Long studenteId);
}
