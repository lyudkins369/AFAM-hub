package it.unipa.afam.service;

import it.unipa.afam.entity.Profilo;
import it.unipa.afam.repository.ProfiloRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class ProfiloService {

    private final ProfiloRepository profiloRepo;

    public ProfiloService(ProfiloRepository profiloRepo) {
        this.profiloRepo = profiloRepo;
    }

    public Map<String, Object> visualizzaProfilo(Long studenteId) {
        Optional<Profilo> p = profiloRepo.findByStudenteId(studenteId);
        if (p.isEmpty()) {
            return Map.of("successo", false, "messaggio", "Nessun dato disponibile");
        }
        Profilo pr = p.get();
        Map<String, Object> result = new HashMap<>();
        result.put("successo", true);
        result.put("id", pr.getId());
        result.put("studenteId", pr.getStudente().getId());
        result.put("nomeUtente", pr.getNomeUtente());
        result.put("nome", pr.getStudente().getNome());
        result.put("cognome", pr.getStudente().getCognome());
        result.put("emailPersonale", pr.getStudente().getEmailPersonale());
        result.put("istituzioneAfam", pr.getIstituzioneAfam());
        result.put("corsoStudi", pr.getCorsoStudi());
        result.put("annoAccademico", pr.getAnnoAccademico());
        result.put("annoDiCorso", pr.getAnnoDiCorso());
        result.put("areaDisciplinare", pr.getAreaDisciplinare());
        result.put("haFoto", pr.getFotoContenuto() != null);
        return result;
    }

    @Transactional
    public Map<String, Object> modificaProfilo(Long studenteId, Map<String, String> dati) {
        Optional<Profilo> p = profiloRepo.findByStudenteId(studenteId);
        if (p.isEmpty()) {
            return Map.of("successo", false, "messaggio", "Profilo non trovato");
        }
        Profilo pr = p.get();
        if (dati.containsKey("nomeUtente")) pr.setNomeUtente(dati.get("nomeUtente"));
        if (dati.containsKey("emailPersonale")) pr.getStudente().setEmailPersonale(dati.get("emailPersonale"));
        if (dati.containsKey("istituzioneAfam")) pr.setIstituzioneAfam(dati.get("istituzioneAfam"));
        if (dati.containsKey("corsoStudi")) pr.setCorsoStudi(dati.get("corsoStudi"));
        if (dati.containsKey("annoAccademico")) pr.setAnnoAccademico(dati.get("annoAccademico"));
        if (dati.containsKey("annoDiCorso")) {
            try { pr.setAnnoDiCorso(Integer.parseInt(dati.get("annoDiCorso"))); }
            catch (NumberFormatException e) { return Map.of("successo", false, "messaggio", "Dati non validi"); }
        }
        if (dati.containsKey("areaDisciplinare")) pr.setAreaDisciplinare(dati.get("areaDisciplinare"));
        profiloRepo.save(pr);
        return Map.of("successo", true, "messaggio", "Modifiche aggiornate correttamente");
    }

    @Transactional
    public Map<String, Object> caricaFoto(Long studenteId, MultipartFile file) {
        if (file == null || file.isEmpty())
            return Map.of("successo", false, "messaggio", "Nessun file selezionato");
        Optional<Profilo> p = profiloRepo.findByStudenteId(studenteId);
        if (p.isEmpty()) return Map.of("successo", false, "messaggio", "Profilo non trovato");
        byte[] bytes;
        try { bytes = file.getBytes(); }
        catch (IOException e) { return Map.of("successo", false, "messaggio", "Errore nel caricamento del file"); }
        Profilo pr = p.get();
        pr.setFotoProfilo(file.getOriginalFilename());
        pr.setFotoContenuto(bytes);
        pr.setFotoContentType(file.getContentType());
        profiloRepo.save(pr);
        return Map.of("successo", true, "messaggio", "Foto aggiornata");
    }
}
