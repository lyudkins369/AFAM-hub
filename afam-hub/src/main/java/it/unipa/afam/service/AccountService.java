package it.unipa.afam.service;

import it.unipa.afam.entity.*;
import it.unipa.afam.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * AccountService - Accesso al Sistema (RAD).
 * 2FA: OTP via Twilio Verify se il numero e' internazionale (+39...) e Twilio e' configurato;
 * altrimenti (o se l'invio fallisce) codice demo "123456". Recupero e modifica password usano il demo.
 */
@Service
public class AccountService {

    private final AccountRepository accountRepo;
    private final StudenteRepository studenteRepo;
    private final ProfiloRepository profiloRepo;
    private final CategoriaRepository categoriaRepo;
    private final VerifyService verifyService;

    private final Map<String, Long> sessioni = new HashMap<>();
    private final Map<String, String> codici2FA = new HashMap<>();
    private final Map<String, String> numeriVerify = new HashMap<>();

    public AccountService(AccountRepository accountRepo, StudenteRepository studenteRepo,
                          ProfiloRepository profiloRepo, CategoriaRepository categoriaRepo,
                          VerifyService verifyService) {
        this.accountRepo = accountRepo;
        this.studenteRepo = studenteRepo;
        this.profiloRepo = profiloRepo;
        this.categoriaRepo = categoriaRepo;
        this.verifyService = verifyService;
    }

    @Transactional
    public Map<String, Object> registra(Map<String, String> dati) {
        String email = dati.get("emailIstituzionale");
        if (email == null || email.isBlank())
            return Map.of("successo", false, "messaggio", "Dati non validi o account gia' esistente");
        if (accountRepo.existsByEmailIstituzionale(email))
            return Map.of("successo", false, "messaggio", "Dati non validi o account gia' esistente");

        Studente s = new Studente(
            dati.getOrDefault("nome", ""),
            dati.getOrDefault("cognome", ""),
            dati.getOrDefault("codiceFiscale", ""),
            dati.getOrDefault("emailPersonale", ""),
            dati.getOrDefault("cellulare", "")
        );
        studenteRepo.save(s);
        accountRepo.save(new Account(email, dati.getOrDefault("password", ""), s));
        profiloRepo.save(new Profilo(s.getNome() + " " + s.getCognome(), s));
        categoriaRepo.save(new Categoria("Altro - Uncategorized", s));

        inviaOtp(email, s.getCellulare());
        return Map.of("successo", true, "messaggio", "Verifica 2FA richiesta", "richiedi2FA", true, "email", email);
    }

    public Map<String, Object> verifica2FA(String email, String codice) {
        if (!verificaCodice(email, codice))
            return Map.of("successo", false, "messaggio", "Codice non valido");
        Optional<Account> acc = accountRepo.findByEmailIstituzionale(email);
        if (acc.isEmpty())
            return Map.of("successo", false, "messaggio", "Account non trovato");
        String token = UUID.randomUUID().toString();
        sessioni.put(token, acc.get().getStudente().getId());
        return Map.of("successo", true, "messaggio", "Autenticazione completata", "token", token,
                       "studenteId", acc.get().getStudente().getId());
    }

    public Map<String, Object> autentica(String email, String password) {
        Optional<Account> acc = accountRepo.findByEmailIstituzionale(email);
        if (acc.isEmpty() || !acc.get().verificaPassword(password))
            return Map.of("successo", false, "messaggio", "Credenziali non valide");
        inviaOtp(email, acc.get().getStudente().getCellulare());
        return Map.of("successo", true, "messaggio", "Verifica 2FA richiesta", "richiedi2FA", true, "email", email);
    }

    public Map<String, Object> recuperaCredenziali(String email) {
        if (!accountRepo.existsByEmailIstituzionale(email))
            return Map.of("successo", false, "messaggio", "Account non trovato");
        numeriVerify.remove(email);
        codici2FA.put(email, "123456");
        return Map.of("successo", true, "messaggio", "Verifica 2FA richiesta", "richiedi2FA", true, "email", email, "recupero", true);
    }

    @Transactional
    public Map<String, Object> modificaPassword(String token, String nuovaPassword, String confermaPassword) {
        Long studenteId = sessioni.get(token);
        if (studenteId == null) return Map.of("successo", false, "messaggio", "Sessione non valida");
        if (nuovaPassword == null || nuovaPassword.length() < 6 || !nuovaPassword.equals(confermaPassword))
            return Map.of("successo", false, "messaggio", "Password non valida");
        Studente s = studenteRepo.findById(studenteId).orElse(null);
        if (s == null || s.getAccount() == null) return Map.of("successo", false, "messaggio", "Account non trovato");
        s.getAccount().aggiornaPassword(nuovaPassword);
        accountRepo.save(s.getAccount());
        return Map.of("successo", true, "messaggio", "Password aggiornata");
    }

    @Transactional
    public Map<String, Object> resetPassword(String email, String nuovaPassword, String confermaPassword) {
        if (nuovaPassword == null || nuovaPassword.length() < 6 || !nuovaPassword.equals(confermaPassword))
            return Map.of("successo", false, "messaggio", "Password non valida");
        Optional<Account> acc = accountRepo.findByEmailIstituzionale(email);
        if (acc.isEmpty()) return Map.of("successo", false, "messaggio", "Account non trovato");
        acc.get().aggiornaPassword(nuovaPassword);
        accountRepo.save(acc.get());
        return Map.of("successo", true, "messaggio", "Credenziali aggiornate");
    }

    public Map<String, Object> richiedi2FAModificaPassword(String token) {
        Long studenteId = sessioni.get(token);
        if (studenteId == null) return Map.of("successo", false, "messaggio", "Sessione non valida");
        Studente s = studenteRepo.findById(studenteId).orElse(null);
        if (s == null || s.getAccount() == null) return Map.of("successo", false, "messaggio", "Account non trovato");
        String email = s.getAccount().getEmailIstituzionale();
        numeriVerify.remove(email);
        codici2FA.put(email, "123456");
        return Map.of("successo", true, "email", email, "richiedi2FA", true);
    }

    public void logout(String token) { sessioni.remove(token); }
    public Long getStudenteIdFromToken(String token) { return sessioni.get(token); }
    public boolean isAuthenticated(String token) { return token != null && sessioni.containsKey(token); }

    private void inviaOtp(String email, String cellulare) {
        if (cellulare != null && cellulare.startsWith("+")) {
            try {
                verifyService.invia(cellulare);
                numeriVerify.put(email, cellulare);
                codici2FA.remove(email);
                return;
            } catch (Exception e) {
                System.err.println("Invio OTP fallito, uso codice demo: " + e.getMessage());
            }
        }
        numeriVerify.remove(email);
        codici2FA.put(email, "123456");
    }

    private boolean verificaCodice(String email, String codice) {
        String numero = numeriVerify.get(email);
        if (numero != null) {
            try {
                boolean ok = verifyService.verifica(numero, codice);
                if (ok) numeriVerify.remove(email);
                return ok;
            } catch (Exception e) {
                System.err.println("Verifica OTP fallita: " + e.getMessage());
                return false;
            }
        }
        String atteso = codici2FA.get(email);
        if (atteso != null && atteso.equals(codice)) { codici2FA.remove(email); return true; }
        return false;
    }
}
