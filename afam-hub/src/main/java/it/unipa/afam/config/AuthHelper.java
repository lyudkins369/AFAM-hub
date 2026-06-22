package it.unipa.afam.config;

import it.unipa.afam.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Utility per estrarre il token di sessione dalla richiesta HTTP e risolvere lo Studente autenticato.
 * Il token viene inviato dal client nell'header "Authorization: Bearer <token>".
 */
@Component
public class AuthHelper {

    private final AccountService accountService;

    public AuthHelper(AccountService accountService) {
        this.accountService = accountService;
    }

    /** Estrae il token grezzo dall'header Authorization. */
    public String token(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || header.isBlank()) return null;
        if (header.startsWith("Bearer ")) return header.substring(7).trim();
        return header.trim();
    }

    /** Restituisce l'id dello Studente autenticato, oppure null se la sessione non è valida. */
    public Long studenteId(HttpServletRequest request) {
        String token = token(request);
        if (token == null || !accountService.isAuthenticated(token)) return null;
        return accountService.getStudenteIdFromToken(token);
    }
}
