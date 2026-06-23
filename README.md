# AFAM Hub — Sistema di Gestione dell'Identità Digitale AFAM

App full-stack per studenti AFAM: profilo, risorse artistiche, condivisione tramite preset e link, ricerca da parte di esaminatori esterni.

- **Backend:** Java 17 · Spring Boot 3.4.5 (Web, Data JPA)
- **Database:** MySQL (i file caricati sono salvati nel DB, quindi accessibili da ogni dispositivo)
- **Frontend:** HTML + CSS + JavaScript (vanilla), chiamate REST via `fetch`
- **2FA:** OTP via Twilio Verify (opzionale) con fallback al codice demo `123456`

## Prerequisiti

- JDK 17+
- Maven 3.8+
- MySQL 8+ in esecuzione

## Avvio rapido (locale, consigliato per l'esame)

1. Crea il database (le tabelle si generano da sole all'avvio):

   ```sql
   CREATE DATABASE afam_hub CHARACTER SET utf8mb4;
   ```

2. Se la tua password di MySQL non è vuota, impostala in `src/main/resources/application.properties` (`spring.datasource.password`).

3. Avvia:

   ```bash
   mvn spring-boot:run
   ```

4. Apri il browser:
   - Studente: http://localhost:8080/
   - Esaminatore Esterno: http://localhost:8080/view.html

In fase di registrazione, se Twilio non è configurato il codice 2FA è **123456**.

## Importare dati esistenti

Se hai un dump (`afam_hub.sql`) da un'altra installazione:

```bash
mysql -u root -p afam_hub < afam_hub.sql
```

## Database in cloud (multi-dispositivo, opzionale)

Per condividere gli stessi dati fra più computer, usa un MySQL gestito (es. Railway) e nel
`application.properties` commenta le righe `spring.datasource.*` locali e abilita quelle cloud
indicate nel file, inserendo host, porta e password del tuo database.

## Twilio (SMS reali, opzionale)

1. Crea un account Twilio e un **Verify Service** (Console → Verify → Services → Create).
2. Verifica i numeri destinatari (Verified Caller IDs) se sei in trial.
3. In `application.properties` inserisci `twilio.sid` (Account SID, `AC...`), `twilio.token`
   (Auth Token), `twilio.from` (numero Twilio) e `twilio.verify.service` (`VA...`).

Se i campi restano segnaposto, l'app continua a funzionare usando il codice demo `123456`.
L'OTP via SMS viene usato quando il numero dello studente è in formato internazionale (`+39...`).

## Struttura

```
src/main/java/it/unipa/afam/
├── entity/        Modello JPA (Studente, Account, Profilo, Risorsa+sottotipi, Categoria,
│                  Sottocategoria, Preset, Link). Risorsa e Profilo includono il file (BLOB).
├── repository/    Spring Data JPA
├── service/       AccountService (auth+2FA via VerifyService), ProfiloService, RisorseService,
│                  PresetService (+ ricerca studenti), LinkService, VerifyService, SmsService
├── controller/    AccountController, ProfiloController, RisorseController, PresetController,
│                  LinkController, PublicController (esaminatore), FileController (serve i file dal DB)
└── config/        AuthHelper (token Bearer), WebConfig (CORS)

src/main/resources/static/   Frontend: index.html (accesso), app.html (studente),
                             view.html (esaminatore), css/, js/
```

## Note

- Le sessioni sono in memoria (token UUID) — riavviando l'app gli utenti restano nel DB ma vanno ri-loggati.
- I file (risorse e foto profilo) sono memorizzati nel database come BLOB: si vedono da qualsiasi
  dispositivo collegato allo stesso database. Limite per file impostato a 50MB.
- Le password sono cifrate con BCrypt.
