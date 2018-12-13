package fr.iolabs.leaf.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import fr.iolabs.leaf.common.errors.InternalServerErrorException;

@Service
public class LeafEmailService {

    @Value("${mailgun.api.url}")
    String mailGunAPIUrl;

    @Value("${mailgun.api.from}")
    String mailGunAPIFrom;

    @Value("${mailgun.api.key}")
    String mailGunAPIKey;

    public void sendEmail(String to, String subject, String text, String html) {
        try {
            Unirest.post(this.mailGunAPIUrl)
                    .basicAuth("api", this.mailGunAPIKey)
                    .field("from", this.mailGunAPIFrom)
                    .field("to", to)
                    .field("subject", subject)
                    .field("text", text)
                    .field("html", html)
                    .asString();
        } catch (UnirestException e) {
            throw new InternalServerErrorException();
        }
    }
}
