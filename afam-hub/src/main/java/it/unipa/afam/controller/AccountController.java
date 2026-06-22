package it.unipa.afam.controller;

import it.unipa.afam.config.AuthHelper;
import it.unipa.afam.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AccountController — Accesso al Sistema (RAD §Accesso al Sistema).
 * Registrazione, Autenticazione, 2FA, Recupero e Modifica Password, Logout.
 */
@RestController
@RequestMapping("/api/auth")
public class AccountController {

    private final AccountService accountService;
    private final AuthHelper auth;

    public AccountController(AccountService accountService, AuthHelper auth) {
        this.accountService = accountService;
        this.auth = auth;
    }

    /** Registrazione nuovo Studente (Registration0101). */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> dati) {
        return ResponseEntity.ok(accountService.registra(dati));
    }

    /** Autenticazione tramite credenziali (Authentication0102) → richiede 2FA. */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> dati) {
        return ResponseEntity.ok(accountService.autentica(
                dati.get("emailIstituzionale"), dati.get("password")));
    }

    /** Verifica codice 2FA → restituisce token di sessione. */
    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verify2fa(@RequestBody Map<String, String> dati) {
        return ResponseEntity.ok(accountService.verifica2FA(
                dati.get("email"), dati.get("codice")));
    }

    /** Recupero credenziali (CredentialRecovery0103) → richiede 2FA. */
    @PostMapping("/recover")
    public ResponseEntity<?> recover(@RequestBody Map<String, String> dati) {
        return ResponseEntity.ok(accountService.recuperaCredenziali(dati.get("email")));
    }

    /** Reset password dopo recupero credenziali. */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> dati) {
        return ResponseEntity.ok(accountService.resetPassword(
                dati.get("email"), dati.get("nuovaPassword"), dati.get("confermaPassword")));
    }

    /** Richiede l'invio del codice 2FA per la modifica password (studente autenticato). */
    @PostMapping("/change-password/request-2fa")
    public ResponseEntity<?> requestChangePassword2fa(HttpServletRequest request) {
        String token = auth.token(request);
        return ResponseEntity.ok(accountService.richiedi2FAModificaPassword(token));
    }

    /** Modifica password (ChangePassword0104) dopo verifica 2FA. */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(HttpServletRequest request, @RequestBody Map<String, String> dati) {
        String token = auth.token(request);
        return ResponseEntity.ok(accountService.modificaPassword(
                token, dati.get("nuovaPassword"), dati.get("confermaPassword")));
    }

    /** Logout. */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        accountService.logout(auth.token(request));
        return ResponseEntity.ok(Map.of("successo", true, "messaggio", "Sessione terminata"));
    }

    /** Verifica stato della sessione corrente. */
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        Long studenteId = auth.studenteId(request);
        if (studenteId == null) {
            return ResponseEntity.ok(Map.of("autenticato", false));
        }
        return ResponseEntity.ok(Map.of("autenticato", true, "studenteId", studenteId));
    }
}
