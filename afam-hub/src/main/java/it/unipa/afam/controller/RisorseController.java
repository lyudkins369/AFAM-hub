package it.unipa.afam.controller;

import it.unipa.afam.config.AuthHelper;
import it.unipa.afam.service.RisorseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * RisorseController — Gestione Risorse (RAD §Gestione Risorse).
 * Visualizzazione, Inserimento, Modifica, Eliminazione di Risorse, Sezioni (Categorie) e Cartelle (Sottocategorie).
 */
@RestController
@RequestMapping("/api/risorse")
public class RisorseController {

    private final RisorseService risorseService;
    private final AuthHelper auth;

    public RisorseController(RisorseService risorseService, AuthHelper auth) {
        this.risorseService = risorseService;
        this.auth = auth;
    }

    // ---------- Risorse ----------
    @GetMapping
    public ResponseEntity<?> visualizza(HttpServletRequest request) {
        Long id = auth.studenteId(request);
        if (id == null) return na();
        return ResponseEntity.ok(risorseService.visualizzaRisorse(id));
    }

    @PostMapping
    public ResponseEntity<?> inserisci(HttpServletRequest request,
                                       @RequestParam("file") MultipartFile file,
                                       @RequestParam("nome") String nome,
                                       @RequestParam(value = "categoriaId", required = false) Long categoriaId,
                                       @RequestParam(value = "sottocategoriaId", required = false) Long sottocategoriaId) {
        Long id = auth.studenteId(request);
        if (id == null) return na();
        return ResponseEntity.ok(risorseService.inserisciRisorsa(id, file, nome, categoriaId, sottocategoriaId));
    }

    @PutMapping("/{risorsaId}")
    public ResponseEntity<?> modifica(HttpServletRequest request, @PathVariable Long risorsaId,
                                      @RequestBody Map<String, Object> dati) {
        Long id = auth.studenteId(request);
        if (id == null) return na();
        return ResponseEntity.ok(risorseService.modificaRisorsa(id, risorsaId, dati));
    }

    @DeleteMapping("/{risorsaId}")
    public ResponseEntity<?> elimina(HttpServletRequest request, @PathVariable Long risorsaId) {
        Long id = auth.studenteId(request);
        if (id == null) return na();
        return ResponseEntity.ok(risorseService.eliminaRisorsa(id, risorsaId));
    }

    // ---------- Sezioni (Categorie) ----------
    @PostMapping("/sezioni")
    public ResponseEntity<?> creaSezione(HttpServletRequest request, @RequestBody Map<String, String> dati) {
        Long id = auth.studenteId(request);
        if (id == null) return na();
        return ResponseEntity.ok(risorseService.creaSezione(id, dati.get("nome")));
    }

    @PutMapping("/sezioni/{categoriaId}")
    public ResponseEntity<?> modificaSezione(HttpServletRequest request, @PathVariable Long categoriaId,
                                             @RequestBody Map<String, String> dati) {
        Long id = auth.studenteId(request);
        if (id == null) return na();
        return ResponseEntity.ok(risorseService.modificaSezione(id, categoriaId, dati.get("nome")));
    }

    @DeleteMapping("/sezioni/{categoriaId}")
    public ResponseEntity<?> eliminaSezione(HttpServletRequest request, @PathVariable Long categoriaId) {
        Long id = auth.studenteId(request);
        if (id == null) return na();
        return ResponseEntity.ok(risorseService.eliminaSezione(id, categoriaId));
    }

    // ---------- Cartelle (Sottocategorie) ----------
    @PostMapping("/sezioni/{categoriaId}/cartelle")
    public ResponseEntity<?> creaCartella(HttpServletRequest request, @PathVariable Long categoriaId,
                                          @RequestBody Map<String, String> dati) {
        Long id = auth.studenteId(request);
        if (id == null) return na();
        return ResponseEntity.ok(risorseService.creaCartella(id, categoriaId, dati.get("nome")));
    }

    @PutMapping("/cartelle/{cartellaId}")
    public ResponseEntity<?> modificaCartella(HttpServletRequest request, @PathVariable Long cartellaId,
                                              @RequestBody Map<String, Object> dati) {
        Long id = auth.studenteId(request);
        if (id == null) return na();
        return ResponseEntity.ok(risorseService.modificaCartella(id, cartellaId, dati));
    }

    @DeleteMapping("/cartelle/{cartellaId}")
    public ResponseEntity<?> eliminaCartella(HttpServletRequest request, @PathVariable Long cartellaId) {
        Long id = auth.studenteId(request);
        if (id == null) return na();
        return ResponseEntity.ok(risorseService.eliminaCartella(id, cartellaId));
    }

    private ResponseEntity<?> na() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("successo", false, "messaggio", "Sessione non valida"));
    }
}
