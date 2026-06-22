package it.unipa.afam.service;

import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * VerifyService — invio e verifica OTP tramite Twilio Verify.
 * Aggira la registrazione A2P 10DLC usando i mittenti gestiti da Twilio.
 */
@Service
public class VerifyService {

    @Value("${twilio.sid:}") private String sid;
    @Value("${twilio.token:}") private String token;
    @Value("${twilio.verify.service:}") private String serviceSid;

    @PostConstruct
    void init() {
        if (sid != null && sid.startsWith("AC")) {
            Twilio.init(sid, token);
        }
    }

    public void invia(String numero) {
        Verification.creator(serviceSid, numero, "sms").create();
    }

    public boolean verifica(String numero, String codice) {
        VerificationCheck check = VerificationCheck.creator(serviceSid)
            .setTo(numero).setCode(codice).create();
        return "approved".equals(check.getStatus());
    }
}
