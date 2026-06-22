package it.unipa.afam.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

@Service
public class SmsService {
    @Value("${twilio.sid}") private String sid;
    @Value("${twilio.token}") private String token;
    @Value("${twilio.from}") private String from;

    @PostConstruct
    void init() { Twilio.init(sid, token); }

    public void invia(String numero, String codice) {
        Message.creator(
            new PhoneNumber(numero),               // es. +39333...
            new PhoneNumber(from),
            "Il tuo codice AFAM Hub è: " + codice
        ).create();
    }
}