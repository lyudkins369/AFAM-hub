package it.unipa.afam.service;

import it.unipa.afam.entity.*;
import it.unipa.afam.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RisorseService {

    private final RisorsaRepository risorsaRepo;
    private final CategoriaRepository categoriaRepo;
    private final SottocategoriaRepository sottocategoriaRepo;
    private final StudenteRepository studenteRepo;

    public RisorseService(RisorsaRepository risorsaRepo, CategoriaRepository categoriaRepo,
                          SottocategoriaRepository sottocategoriaRepo, StudenteRepository studenteRepo) {
        this.risorsaRepo = risorsaRepo;
        this.categoriaRepo = categoriaRepo;
        this.sottocategoriaRepo = sottocategoriaRepo;
        this.studenteRepo = studenteRepo;
    }

    public Map<String, Object> visualizzaRisorse(Long studenteId) {
        List<Categoria> categorie = categoriaRepo.findByStudenteIdOrderByNomeAsc(studenteId);
        List<Map<String, Object>> sezioni = new ArrayList<>();
        for (Categoria cat : categorie) {
            Map<String, Object> sezione = new HashMap<>();
            sezione.put("id", cat.getId());
            sezione.put("nome", cat.getNome());
            List<Map<String, Object>> cartelle = cat.getSottocategorie().stream().map(sc -> {
                Map<String, Object> cartella = new HashMap<>();
                cartella.put("id", sc.getId());
                cartella.put("nome", sc.getNome());
                cartella.put("risorse", sc.getRisorse().stream().map(this::risorsaToMap).collect(Collectors.toList()));
                return cartella;
            }).collect(Collectors.toList());
            sezione.put("cartelle", cartelle);
            List<Map<String, Object>> risorseLibere = cat.getRisorse().stream()
                .filter(r -> r.getSottocategoria() == null)
                .map(this::risorsaToMap)
                .collect(Collectors.toList());
            sezione.put("risorse", risorseLibere);
            sezioni.add(sezione);
        }
        return Map.of("successo", true, "sezioni", sezioni);
    }

    private Map<String, Object> risorsaToMap(Risorsa r) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", r.getId());
        m.put("nome", r.getNome());
        m.put("descrizione", r.getDescrizione());
        m.put("tipo", r.getTipo().name());
        m.put("percorsoFile", r.getPercorsoFile());
        m.put("dataCaricamento", r.getDataCaricamento());
        if (r.getCategoria() != null) {
            m.put("categoriaId", r.getCategoria().getId());
            m.put("categoriaNome", r.getCategoria().getNome());
        }
        if (r.getSottocategoria() != null) {
            m.put("sottocategoriaId", r.getSottocategoria().getId());
            m.put("sottocategoriaNome", r.getSottocategoria().getNome());
        }
        return m;
    }

    @Transactional
    public Map<String, Object> inserisciRisorsa(Long studenteId, MultipartFile file, String nome,
                                                 Long categoriaId, Long sottocategoriaId) {
        if (file == null || file.isEmpty() || nome == null || nome.isBlank())
            return Map.of("successo", false, "messaggio", "Dati non validi");
        Studente s = studenteRepo.findById(studenteId).orElse(null);
        if (s == null) return Map.of("successo", false, "messaggio", "Studente non trovato");
        Categoria cat = categoriaId != null ? categoriaRepo.findById(categoriaId).orElse(null) : null;
        Sottocategoria scat = sottocategoriaId != null ? sottocategoriaRepo.findById(sottocategoriaId).orElse(null) : null;

        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
            ? originalName.substring(originalName.lastIndexOf(".")).toLowerCase() : "";
        byte[] bytes;
        try { bytes = file.getBytes(); }
        catch (IOException e) { return Map.of("successo", false, "messaggio", "Errore nel caricamento del file"); }

        TipoRisorsa tipo = determinaTipo(ext);
        Risorsa risorsa;
        switch (tipo) {
            case IMMAGINE -> risorsa = new Immagine();
            case VIDEO -> risorsa = new Video();
            case AUDIO -> risorsa = new Audio();
            default -> risorsa = new Documento();
        }
        risorsa.setNome(nome);
        risorsa.setPercorsoFile(originalName != null ? originalName : "file" + ext);
        risorsa.setContenuto(bytes);
        risorsa.setContentType(file.getContentType());
        risorsa.setDataCaricamento(new Date());
        risorsa.setStudente(s);
        risorsa.setCategoria(cat);
        risorsa.setSottocategoria(scat);
        risorsaRepo.save(risorsa);
        return Map.of("successo", true, "messaggio", "Caricamento completato correttamente");
    }

    private TipoRisorsa determinaTipo(String ext) {
        return switch (ext) {
            case ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".tiff", ".tff", ".webp", ".svg" -> TipoRisorsa.IMMAGINE;
            case ".mp4", ".avi", ".mov", ".mkv", ".flv", ".wmv" -> TipoRisorsa.VIDEO;
            case ".mp3", ".wav", ".wma", ".aac", ".flac", ".ogg" -> TipoRisorsa.AUDIO;
            default -> TipoRisorsa.DOCUMENTO;
        };
    }

    @Transactional
    public Map<String, Object> modificaRisorsa(Long studenteId, Long risorsaId, Map<String, Object> dati) {
        Risorsa r = risorsaRepo.findById(risorsaId).orElse(null);
        if (r == null || !r.getStudente().getId().equals(studenteId))
            return Map.of("successo", false, "messaggio", "Risorsa non trovata");
        if (dati.containsKey("nome")) r.setNome((String) dati.get("nome"));
        if (dati.containsKey("descrizione")) r.setDescrizione((String) dati.get("descrizione"));
        if (dati.containsKey("categoriaId")) {
            Object catId = dati.get("categoriaId");
            if (catId != null) r.setCategoria(categoriaRepo.findById(Long.valueOf(catId.toString())).orElse(null));
        }
        if (dati.containsKey("sottocategoriaId")) {
            Object scatId = dati.get("sottocategoriaId");
            if (scatId != null) r.setSottocategoria(sottocategoriaRepo.findById(Long.valueOf(scatId.toString())).orElse(null));
            else r.setSottocategoria(null);
        }
        risorsaRepo.save(r);
        return Map.of("successo", true, "messaggio", "Modifica avvenuta correttamente");
    }

    @Transactional
    public Map<String, Object> eliminaRisorsa(Long studenteId, Long risorsaId) {
        Risorsa r = risorsaRepo.findById(risorsaId).orElse(null);
        if (r == null || !r.getStudente().getId().equals(studenteId))
            return Map.of("successo", false, "messaggio", "Eliminazione non valida");
        risorsaRepo.delete(r);
        return Map.of("successo", true, "messaggio", "Eliminazione avvenuta correttamente");
    }

    @Transactional
    public Map<String, Object> creaSezione(Long studenteId, String nome) {
        if (nome == null || nome.isBlank()) return Map.of("successo", false, "messaggio", "Dati non validi");
        Studente s = studenteRepo.findById(studenteId).orElse(null);
        if (s == null) return Map.of("successo", false, "messaggio", "Studente non trovato");
        categoriaRepo.save(new Categoria(nome, s));
        return Map.of("successo", true, "messaggio", "Creazione avvenuta correttamente");
    }

    @Transactional
    public Map<String, Object> modificaSezione(Long studenteId, Long categoriaId, String nome) {
        Categoria c = categoriaRepo.findById(categoriaId).orElse(null);
        if (c == null || !c.getStudente().getId().equals(studenteId))
            return Map.of("successo", false, "messaggio", "Modifica non valida");
        if (nome == null || nome.isBlank()) return Map.of("successo", false, "messaggio", "Modifica non valida");
        c.setNome(nome);
        categoriaRepo.save(c);
        return Map.of("successo", true, "messaggio", "Modifica avvenuta correttamente");
    }

    @Transactional
    public Map<String, Object> eliminaSezione(Long studenteId, Long categoriaId) {
        Categoria c = categoriaRepo.findById(categoriaId).orElse(null);
        if (c == null || !c.getStudente().getId().equals(studenteId))
            return Map.of("successo", false, "messaggio", "Eliminazione non valida");
        Categoria altro = categoriaRepo.findByStudenteIdOrderByNomeAsc(studenteId).stream()
            .filter(cat -> cat.getNome().equals("Altro - Uncategorized")).findFirst().orElse(null);
        if (altro != null && !altro.getId().equals(categoriaId)) {
            for (Risorsa r : c.getRisorse()) { r.setCategoria(altro); r.setSottocategoria(null); risorsaRepo.save(r); }
            for (Sottocategoria sc : c.getSottocategorie())
                for (Risorsa r : sc.getRisorse()) { r.setCategoria(altro); r.setSottocategoria(null); risorsaRepo.save(r); }
        }
        categoriaRepo.delete(c);
        return Map.of("successo", true, "messaggio", "Eliminazione avvenuta correttamente");
    }

    @Transactional
    public Map<String, Object> creaCartella(Long studenteId, Long categoriaId, String nome) {
        if (nome == null || nome.isBlank()) return Map.of("successo", false, "messaggio", "Dati non validi");
        Categoria c = categoriaRepo.findById(categoriaId).orElse(null);
        if (c == null || !c.getStudente().getId().equals(studenteId))
            return Map.of("successo", false, "messaggio", "Sezione non trovata");
        sottocategoriaRepo.save(new Sottocategoria(nome, c));
        return Map.of("successo", true, "messaggio", "Creazione avvenuta correttamente");
    }

    @Transactional
    public Map<String, Object> modificaCartella(Long studenteId, Long cartellaId, Map<String, Object> dati) {
        Sottocategoria sc = sottocategoriaRepo.findById(cartellaId).orElse(null);
        if (sc == null || !sc.getCategoria().getStudente().getId().equals(studenteId))
            return Map.of("successo", false, "messaggio", "Modifica non valida");
        if (dati.containsKey("nome")) sc.setNome((String) dati.get("nome"));
        if (dati.containsKey("categoriaId")) {
            Categoria newCat = categoriaRepo.findById(Long.valueOf(dati.get("categoriaId").toString())).orElse(null);
            if (newCat != null) sc.setCategoria(newCat);
        }
        sottocategoriaRepo.save(sc);
        return Map.of("successo", true, "messaggio", "Modifica avvenuta correttamente");
    }

    @Transactional
    public Map<String, Object> eliminaCartella(Long studenteId, Long cartellaId) {
        Sottocategoria sc = sottocategoriaRepo.findById(cartellaId).orElse(null);
        if (sc == null || !sc.getCategoria().getStudente().getId().equals(studenteId))
            return Map.of("successo", false, "messaggio", "Eliminazione non valida");
        Categoria parentCat = sc.getCategoria();
        for (Risorsa r : sc.getRisorse()) { r.setSottocategoria(null); r.setCategoria(parentCat); risorsaRepo.save(r); }
        sottocategoriaRepo.delete(sc);
        return Map.of("successo", true, "messaggio", "Eliminazione avvenuta correttamente");
    }
}
