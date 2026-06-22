package it.unipa.afam.controller;

import it.unipa.afam.entity.Profilo;
import it.unipa.afam.entity.Risorsa;
import it.unipa.afam.repository.ProfiloRepository;
import it.unipa.afam.repository.RisorsaRepository;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * Serve i file (risorse e foto profilo) leggendoli dal database, cosi' sono accessibili
 * da qualsiasi dispositivo. Accesso pubblico: i contenuti sono condivisi tramite link/preset.
 */
@RestController
@RequestMapping("/api/file")
public class FileController {

    private final RisorsaRepository risorsaRepo;
    private final ProfiloRepository profiloRepo;

    public FileController(RisorsaRepository risorsaRepo, ProfiloRepository profiloRepo) {
        this.risorsaRepo = risorsaRepo;
        this.profiloRepo = profiloRepo;
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> risorsa(@PathVariable Long id) {
        Risorsa r = risorsaRepo.findById(id).orElse(null);
        if (r == null || r.getContenuto() == null) return ResponseEntity.notFound().build();
        return body(r.getContenuto(), r.getContentType(), r.getNome());
    }

    @GetMapping("/foto/{studenteId}")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> foto(@PathVariable Long studenteId) {
        Profilo p = profiloRepo.findByStudenteId(studenteId).orElse(null);
        if (p == null || p.getFotoContenuto() == null) return ResponseEntity.notFound().build();
        return body(p.getFotoContenuto(), p.getFotoContentType(), "foto");
    }

    private ResponseEntity<byte[]> body(byte[] data, String contentType, String nome) {
        MediaType mt = MediaType.APPLICATION_OCTET_STREAM;
        try { if (contentType != null && !contentType.isBlank()) mt = MediaType.parseMediaType(contentType); }
        catch (Exception ignored) {}
        return ResponseEntity.ok()
                .contentType(mt)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nome + "\"")
                .body(data);
    }
}
