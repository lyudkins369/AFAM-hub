package it.unipa.afam.controller;

import it.unipa.afam.config.AuthHelper;
import it.unipa.afam.service.PresetService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * PresetController — Condivisione tramite Preset (RAD §Condivisione).
 */
@RestController
@RequestMapping("/api/preset")
public class PresetController {

    private final PresetService presetService;
    private final AuthHelper auth;

    public PresetController(PresetService presetService, AuthHelper auth) {
        this.presetService = presetService;
        this.auth = auth;
    }

    @GetMapping
    public ResponseEntity<?> visualizza(HttpServletRequest request) {
        Long id = auth.studenteId(request);
        if (id == null) return na();
        return ResponseEntity.ok(presetService.visualizzaPreset(id));
    }

    @GetMapping("/{presetId}")
    public ResponseEntity<?> dettaglio(HttpServletRequest request, @PathVariable Long presetId) {
        Long id = auth.studenteId(request);
        if (id == null) return na();
        return ResponseEntity.ok(presetService.dettaglioPreset(id, presetId));
    }

    @PostMapping
    public ResponseEntity<?> crea(HttpServletRequest request, @RequestBody Map<String, String> dati) {
        Long id = auth.studenteId(request);
        if (id == null) return na();
        return ResponseEntity.ok(presetService.creaPreset(id, dati.get("nome")));
    }

    @PutMapping("/{presetId}/nome")
    public ResponseEntity<?> rinomina(HttpServletRequest request, @PathVariable Long presetId,
                                      @RequestBody Map<String, String> dati) {
        Long id = auth.studenteId(request);
        if (id == null) return na();
        return ResponseEntity.ok(presetService.rinominaPreset(id, presetId, dati.get("nome")));
    }

    @PutMapping("/{presetId}/risorse")
    public ResponseEntity<?> aggiornaRisorse(HttpServletRequest request, @PathVariable Long presetId,
                                             @RequestBody Map<String, List<Long>> dati) {
        Long id = auth.studenteId(request);
        if (id == null) return na();
        return ResponseEntity.ok(presetService.aggiornaRisorse(id, presetId, dati.get("risorseIds")));
    }

    @PutMapping("/{presetId}/pubblico")
    public ResponseEntity<?> pubblico(HttpServletRequest request, @PathVariable Long presetId,
                                      @RequestBody Map<String, Boolean> dati) {
        Long id = auth.studenteId(request);
        if (id == null) return na();
        boolean flag = dati.getOrDefault("pubblico", false);
        return ResponseEntity.ok(presetService.impostaPubblico(id, presetId, flag));
    }

    @DeleteMapping("/{presetId}")
    public ResponseEntity<?> elimina(HttpServletRequest request, @PathVariable Long presetId) {
        Long id = auth.studenteId(request);
        if (id == null) return na();
        return ResponseEntity.ok(presetService.eliminaPreset(id, presetId));
    }

    private ResponseEntity<?> na() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("successo", false, "messaggio", "Sessione non valida"));
    }
}
