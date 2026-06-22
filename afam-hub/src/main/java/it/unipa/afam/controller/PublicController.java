package it.unipa.afam.controller;

import it.unipa.afam.service.LinkService;
import it.unipa.afam.service.PresetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * PublicController - funzioni per l'Esaminatore Esterno (senza autenticazione):
 * ricerca studenti, visualizzazione profilo pubblico di uno studente, apertura link.
 */
@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final PresetService presetService;
    private final LinkService linkService;

    public PublicController(PresetService presetService, LinkService linkService) {
        this.presetService = presetService;
        this.linkService = linkService;
    }

    @GetMapping("/studenti")
    public ResponseEntity<?> cercaStudenti(@RequestParam(value = "q", required = false) String q) {
        return ResponseEntity.ok(presetService.cercaStudenti(q));
    }

    @GetMapping("/studente/{id}")
    public ResponseEntity<?> dettaglioStudente(@PathVariable Long id) {
        return ResponseEntity.ok(presetService.dettaglioStudentePubblico(id));
    }

    @GetMapping("/preset")
    public ResponseEntity<?> cercaPreset(@RequestParam(value = "q", required = false) String q) {
        return ResponseEntity.ok(presetService.cercaPresetPubblici(q));
    }

    @GetMapping("/preset/{presetId}")
    public ResponseEntity<?> dettaglioPreset(@PathVariable Long presetId) {
        return ResponseEntity.ok(presetService.dettaglioPresetPubblico(presetId));
    }

    @GetMapping("/link/{url}")
    public ResponseEntity<?> apriLink(@PathVariable String url) {
        return ResponseEntity.ok(linkService.apriLink(url));
    }
}
