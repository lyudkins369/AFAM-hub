package it.unipa.afam.service;

import it.unipa.afam.entity.*;
import it.unipa.afam.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * LinkService - Gestione dei Link condivisibili (RAD).
 */
@Service
public class LinkService {

    private final LinkRepository linkRepo;
    private final PresetRepository presetRepo;

    public LinkService(LinkRepository linkRepo, PresetRepository presetRepo) {
        this.linkRepo = linkRepo;
        this.presetRepo = presetRepo;
    }

    @Transactional
    public Map<String, Object> creaLink(Long studenteId, Long presetId, String nomeLink) {
        Preset p = presetRepo.findById(presetId).orElse(null);
        if (p == null || !p.getStudente().getId().equals(studenteId))
            return Map.of("successo", false, "messaggio", "Dati non validi");
        if (nomeLink == null || nomeLink.isBlank())
            return Map.of("successo", false, "messaggio", "Dati non validi");
        Link link = p.getLink();
        if (link == null) { link = new Link(p); p.setLink(link); }
        link.setNomeLink(nomeLink.trim());
        linkRepo.save(link);
        return Map.of("successo", true, "messaggio", "Link salvato e copiato",
                "url", link.getUrl(), "nomeLink", link.getNomeLink(),
                "presetNome", p.getNome(), "presetId", p.getId());
    }

    public Map<String, Object> mieiLink(Long studenteId) {
        List<Map<String, Object>> lista = linkRepo.findByPresetStudenteId(studenteId).stream().map(l -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", l.getId());
            m.put("nomeLink", l.getNomeLink());
            m.put("url", l.getUrl());
            m.put("aperto", l.isAperto());
            m.put("dataApertura", l.getDataApertura());
            m.put("dataCreazione", l.getDataCreazione());
            m.put("presetNome", l.getPreset() != null ? l.getPreset().getNome() : null);
            return m;
        }).collect(Collectors.toList());
        if (lista.isEmpty())
            return Map.of("successo", true, "messaggio", "Nessun link disponibile", "link", lista);
        return Map.of("successo", true, "link", lista);
    }

    @Transactional
    public Map<String, Object> apriLink(String url) {
        Link link = linkRepo.findByUrl(url).orElse(null);
        if (link == null) return Map.of("successo", false, "messaggio", "Link non valido");
        link.registraApertura(new Date());
        linkRepo.save(link);

        Preset p = link.getPreset();
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

        List<Map<String, Object>> risorse = p.getRisorse().stream().map(r -> {
            Map<String, Object> rm = new HashMap<>();
            rm.put("id", r.getId());
            rm.put("nome", r.getNome());
            rm.put("descrizione", r.getDescrizione());
            rm.put("tipo", r.getTipo().name());
            rm.put("percorsoFile", r.getPercorsoFile());
            return rm;
        }).collect(Collectors.toList());

        Map<String, Object> preset = new HashMap<>();
        preset.put("id", p.getId());
        preset.put("nome", p.getNome());
        preset.put("profilo", profilo);
        preset.put("risorse", risorse);
        return Map.of("successo", true, "nomeLink", link.getNomeLink(), "preset", preset);
    }
}
