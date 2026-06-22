package it.unipa.afam.controller;

import it.unipa.afam.config.AuthHelper;
import it.unipa.afam.service.LinkService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * LinkController — Gestione dei Link condivisibili (RAD §Condivisione).
 * Crea Link (GenerateLink0406) e Verifica apertura Link ("I miei Link").
 */
@RestController
@RequestMapping("/api/link")
public class LinkController {

    private final LinkService linkService;
    private final AuthHelper auth;

    public LinkController(LinkService linkService, AuthHelper auth) {
        this.linkService = linkService;
        this.auth = auth;
    }

    /** Crea (o aggiorna) il link per un preset. */
    @PostMapping
    public ResponseEntity<?> crea(HttpServletRequest request, @RequestBody Map<String, Object> dati) {
        Long id = auth.studenteId(request);
        if (id == null) return na();
        Long presetId = dati.get("presetId") != null ? Long.valueOf(dati.get("presetId").toString()) : null;
        String nomeLink = dati.get("nomeLink") != null ? dati.get("nomeLink").toString() : null;
        if (presetId == null) {
            return ResponseEntity.ok(Map.of("successo", false, "messaggio", "Dati non validi"));
        }
        return ResponseEntity.ok(linkService.creaLink(id, presetId, nomeLink));
    }

    /** Elenco dei link dello studente con relativo stato di apertura. */
    @GetMapping
    public ResponseEntity<?> mieiLink(HttpServletRequest request) {
        Long id = auth.studenteId(request);
        if (id == null) return na();
        return ResponseEntity.ok(linkService.mieiLink(id));
    }

    private ResponseEntity<?> na() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("successo", false, "messaggio", "Sessione non valida"));
    }
}
