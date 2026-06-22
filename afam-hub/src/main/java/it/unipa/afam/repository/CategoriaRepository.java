package it.unipa.afam.repository;

import it.unipa.afam.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByStudenteIdOrderByNomeAsc(Long studenteId);
}
