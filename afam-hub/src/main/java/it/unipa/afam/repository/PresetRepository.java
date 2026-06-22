package it.unipa.afam.repository;

import it.unipa.afam.entity.Preset;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PresetRepository extends JpaRepository<Preset, Long> {
    List<Preset> findByStudenteId(Long studenteId);
    List<Preset> findByStudenteIdAndPubblicoTrue(Long studenteId);
    List<Preset> findByPubblicoTrue();
}
