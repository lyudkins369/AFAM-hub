package it.unipa.afam.service;

import it.unipa.afam.entity.*;
import it.unipa.afam.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PresetService - Condivisione tramite Preset (RAD) e ricerca pubblica per l'Esaminatore Esterno.
 */
@Service
public class PresetService {

    private final PresetRepository presetRepo;
    private final StudenteRepository studenteRepo;
    private final RisorsaRepository risorsaRepo;
    private final ProfiloRepository profiloRepo;

    public PresetService(PresetRepository presetRepo, StudenteRepository studenteRepo,
                         RisorsaRepository risorsaRepo, ProfiloRepository profiloRepo) {
        this.presetRepo = presetRepo;
        this.studenteRepo = studenteRepo;
        this.risorsaRepo = risorsaRepo;
        this.profiloRepo = profiloRepo;
    }

    public Map<String, Object> visualizzaPreset(Long studenteId) {
        List<Map<String, Object>> lista = presetRepo.findByStudenteId(studenteId).stream()
                .map(this::presetToMap).collect(Collectors.toList());
        return Map.of("successo", true, "preset", lista);
    }

    private Map<String, Object> presetToMap(Preset p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getId());
        m.put("nome", p.getNome());
        m.put("pubblico", p.isPubblico());
        m.put("dataCreazione", p.getDataCreazione());
        m.put("numeroRisorse", p.getRisorse().size());
        m.put("risorseIds", p.getRisorse().stream().map(Risorsa::getId).collect(Collectors.toList()));
        if (p.getLink() != null) {
            Map<String, Object> link = new HashMap<>();
            link.put("id", p.getLink().getId());
            link.put("nomeLink", p.getLink().getNomeLink());
            link.put("url", p.getLink().getUrl());
            link.put("aperto", p.getLink().isAperto());
            m.put("link", link);
        }
        return m;
    }

    @Transactional
    public Map<String, Object> creaPreset(Long studenteId, String nome) {
        if (nome == null || nome.isBlank())
            return Map.of("successo", false, "messaggio", "Dati non validi");
        Studente s = studenteRepo.findById(studenteId).orElse(null);
        if (s == null) return Map.of("successo", false, "messaggio", "Studente non trovato");
        if (s.esistePresetConNome(nome))
            return Map.of("successo", false, "messaggio", "Dati non validi");
        Preset p = new Preset(nome.trim(), s);
        presetRepo.save(p);
        return Map.of("successo", true, "messaggio", "Creazione avvenuta correttamente", "presetId", p.getId());
    }

    @Transactional
    public Map<String, Object> rinominaPreset(Long studenteId, Long presetId, String nome) {
        Preset p = presetRepo.findById(presetId).orElse(null);
        if (p == null || !p.getStudente().getId().equals(studenteId))
            return Map.of("successo", false, "messaggio", "Modifica non valida");
        if (nome == null || nome.isBlank())
            return Map.of("successo", false, "messaggio", "Modifica non valida");
        boolean duplicato = p.getStudente().getPreset().stream()
                .anyMatch(o -> !o.getId().equals(presetId) && o.getNome().equalsIgnoreCase(nome.trim()));
        if (duplicato) return Map.of("successo", false, "messaggio", "Modifica non valida");
        p.setNome(nome.trim());
        presetRepo.save(p);
        return Map.of("successo", true, "messaggio", "Modifica avvenuta correttamente");
    }

    @Transactional
    public Map<String, Object> aggiornaRisorse(Long studenteId, Long presetId, List<Long> risorseIds) {
        Preset p = presetRepo.findById(presetId).orElse(null);
        if (p == null || !p.getStudente().getId().equals(studenteId))
            return Map.of("successo", false, "messaggio", "Modifica non valida");
        if (risorseIds == null)
            return Map.of("successo", false, "messaggio", "Modifica non valida");
        p.getRisorse().clear();
        for (Long rid : risorseIds) {
            Risorsa r = risorsaRepo.findById(rid).orElse(null);
            if (r != null && r.getStudente().getId().equals(studenteId)) p.aggiungiRisorsa(r);
        }
        presetRepo.save(p);
        return Map.of("successo", true, "messaggio", "Modifica avvenuta correttamente");
    }

    @Transactional
    public Map<String, Object> impostaPubblico(Long studenteId, Long presetId, boolean pubblico) {
        Preset p = presetRepo.findById(presetId).orElse(null);
        if (p == null || !p.getStudente().getId().equals(studenteId))
            return Map.of("successo", false, "messaggio", "Modifica non valida");
        p.impostaPubblico(pubblico);
        presetRepo.save(p);
        return Map.of("successo", true, "messaggio", "Modifica avvenuta correttamente", "pubblico", pubblico);
    }

    @Transactional
    public Map<String, Object> eliminaPreset(Long studenteId, Long presetId) {
        Preset p = presetRepo.findById(presetId).orElse(null);
        if (p == null || !p.getStudente().getId().equals(studenteId))
            return Map.of("successo", false, "messaggio", "Eliminazione non valida");
        presetRepo.delete(p);
        return Map.of("successo", true, "messaggio", "Eliminazione avvenuta correttamente");
    }

    public Map<String, Object> dettaglioPreset(Long studenteId, Long presetId) {
        Preset p = presetRepo.findById(presetId).orElse(null);
        if (p == null || !p.getStudente().getId().equals(studenteId))
            return Map.of("successo", false, "messaggio", "Preset non trovato");
        return Map.of("successo", true, "preset", contenutoPreset(p));
    }

    // ---- Esaminatore Esterno: ricerca studenti ----
    public Map<String, Object> cercaStudenti(String criterio) {
        String q = criterio == null ? "" : criterio.trim().toLowerCase();
        List<Map<String, Object>> risultati = studenteRepo.findAll().stream()
            .map(st -> {
                Profilo pr = st.getProfilo();
                long pubbliche = presetRepo.findByStudenteIdAndPubblicoTrue(st.getId()).stream()
                    .flatMap(p -> p.getRisorse().stream()).map(Risorsa::getId).distinct().count();
                Map<String, Object> m = new HashMap<>();
                m.put("studenteId", st.getId());
                m.put("nome", st.getNome());
                m.put("cognome", st.getCognome());
                m.put("autore", pr != null && pr.getNomeUtente() != null ? pr.getNomeUtente() : st.getNome() + " " + st.getCognome());
                m.put("istituzione", pr != null ? pr.getIstituzioneAfam() : null);
                m.put("areaDisciplinare", pr != null ? pr.getAreaDisciplinare() : null);
                m.put("numeroRisorse", pubbliche);
                return m;
            })
            .filter(m -> ((Long) m.get("numeroRisorse")) > 0)
            .filter(m -> {
                if (q.isEmpty()) return true;
                String autore = String.valueOf(m.get("autore")).toLowerCase();
                String ist = m.get("istituzione") == null ? "" : String.valueOf(m.get("istituzione")).toLowerCase();
                return autore.contains(q) || ist.contains(q);
            })
            .collect(Collectors.toList());
        if (risultati.isEmpty())
            return Map.of("successo", true, "messaggio", "Nessuno studente trovato", "studenti", risultati);
        return Map.of("successo", true, "studenti", risultati);
    }

    public Map<String, Object> dettaglioStudentePubblico(Long studenteId) {
        Studente st = studenteRepo.findById(studenteId).orElse(null);
        if (st == null) return Map.of("successo", false, "messaggio", "Studente non trovato");
        Profilo pr = st.getProfilo();
        Map<String, Object> profilo = new HashMap<>();
        profilo.put("nomeUtente", pr != null && pr.getNomeUtente() != null ? pr.getNomeUtente() : st.getNome() + " " + st.getCognome());
        profilo.put("istituzioneAfam", pr != null ? pr.getIstituzioneAfam() : null);
        profilo.put("emailPersonale", st.getEmailPersonale());
        profilo.put("corsoStudi", pr != null ? pr.getCorsoStudi() : null);
        profilo.put("annoAccademico", pr != null ? pr.getAnnoAccademico() : null);
        profilo.put("annoDiCorso", pr != null ? pr.getAnnoDiCorso() : null);
        profilo.put("areaDisciplinare", pr != null ? pr.getAreaDisciplinare() : null);
        Map<Long, Map<String,Object>> risMap = new LinkedHashMap<>();
        for (Preset p : presetRepo.findByStudenteIdAndPubblicoTrue(studenteId)) {
            for (Risorsa r : p.getRisorse()) {
                risMap.computeIfAbsent(r.getId(), k -> {
                    Map<String,Object> rm = new HashMap<>();
                    rm.put("id", r.getId());
                    rm.put("nome", r.getNome());
                    rm.put("descrizione", r.getDescrizione());
                    rm.put("tipo", r.getTipo().name());
                    rm.put("percorsoFile", r.getPercorsoFile());
                    return rm;
                });
            }
        }
        Map<String, Object> studente = new HashMap<>();
        studente.put("nome", st.getNome() + " " + st.getCognome());
        studente.put("profilo", profilo);
        studente.put("risorse", new ArrayList<>(risMap.values()));
        return Map.of("successo", true, "studente", studente);
    }

    public Map<String, Object> cercaPresetPubblici(String criterio) {
        List<Preset> pubblici = presetRepo.findByPubblicoTrue();
        String q = criterio == null ? "" : criterio.trim().toLowerCase();
        List<Map<String, Object>> risultati = pubblici.stream()
            .filter(p -> {
                if (q.isEmpty()) return true;
                Studente s = p.getStudente();
                Profilo pr = s.getProfilo();
                String nomeUtente = pr != null && pr.getNomeUtente() != null ? pr.getNomeUtente() : "";
                return p.getNome().toLowerCase().contains(q)
                        || s.getNome().toLowerCase().contains(q)
                        || s.getCognome().toLowerCase().contains(q)
                        || nomeUtente.toLowerCase().contains(q);
            })
            .map(p -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", p.getId());
                m.put("nome", p.getNome());
                m.put("numeroRisorse", p.getRisorse().size());
                Studente s = p.getStudente();
                Profilo pr = s.getProfilo();
                m.put("autore", pr != null && pr.getNomeUtente() != null ? pr.getNomeUtente() : s.getNome() + " " + s.getCognome());
                return m;
            })
            .collect(Collectors.toList());
        return Map.of("successo", true, "preset", risultati);
    }

    public Map<String, Object> dettaglioPresetPubblico(Long presetId) {
        Preset p = presetRepo.findById(presetId).orElse(null);
        if (p == null || !p.isPubblico())
            return Map.of("successo", false, "messaggio", "Preset non disponibile");
        return Map.of("successo", true, "preset", contenutoPreset(p));
    }

    private Map<String, Object> contenutoPreset(Preset p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getId());
        m.put("nome", p.getNome());
        m.put("pubblico", p.isPubblico());
        Studente s = p.getStudente();
        Profilo pr = s.getProfilo();
        Map<String, Object> profilo = new HashMap<>();
        profilo.put("nomeUtente", pr != null ? pr.getNomeUtente() : s.getNome() + " " + s.getCognome());
        profilo.put("istituzioneAfam", pr != null ? pr.getIstituzioneAfam() : null);
        profilo.put("emailPersonale", s.getEmailPersonale());
        profilo.put("corsoStudi", pr != null ? pr.getCorsoStudi() : null);
        profilo.put("annoAccademico", pr != null ? pr.getAnnoAccademico() : null);
        profilo.put("annoDiCorso", pr != null ? pr.getAnnoDiCorso() : null);
        profilo.put("areaDisciplinare", pr != null ? pr.getAreaDisciplinare() : null);
        m.put("profilo", profilo);
        List<Map<String, Object>> risorse = p.getRisorse().stream().map(r -> {
            Map<String, Object> rm = new HashMap<>();
            rm.put("id", r.getId());
            rm.put("nome", r.getNome());
            rm.put("descrizione", r.getDescrizione());
            rm.put("tipo", r.getTipo().name());
            rm.put("percorsoFile", r.getPercorsoFile());
            return rm;
        }).collect(Collectors.toList());
        m.put("risorse", risorse);
        return m;
    }
}
