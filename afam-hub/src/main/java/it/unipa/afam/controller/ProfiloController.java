package it.unipa.afam.controller;

import it.unipa.afam.config.AuthHelper;
import it.unipa.afam.service.ProfiloService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/profilo")
public class ProfiloController {

    private final ProfiloService profiloService;
    private final AuthHelper auth;

    public ProfiloController(ProfiloService profiloService, AuthHelper auth) {
        this.profiloService = profiloService;
        this.auth = auth;
    }

    @GetMapping
    public ResponseEntity<?> visualizza(HttpServletRequest request) {
        Long studenteId = auth.studenteId(request);
        if (studenteId == null) return nonAutenticato();
        return ResponseEntity.ok(profiloService.visualizzaProfilo(studenteId));
    }

    @PostMapping("/foto")
    public ResponseEntity<?> caricaFoto(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        Long studenteId = auth.studenteId(request);
        if (studenteId == null) return nonAutenticato();
        return ResponseEntity.ok(profiloService.caricaFoto(studenteId, file));
    }

    @PutMapping
    public ResponseEntity<?> modifica(HttpServletRequest request, @RequestBody Map<String, String> dati) {
        Long studenteId = auth.studenteId(request);
        if (studenteId == null) return nonAutenticato();
        return ResponseEntity.ok(profiloService.modificaProfilo(studenteId, dati));
    }

    private ResponseEntity<?> nonAutenticato() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("successo", false, "messaggio", "Sessione non valida"));
    }
}
