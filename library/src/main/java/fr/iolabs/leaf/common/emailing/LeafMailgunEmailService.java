package fr.iolabs.leaf.common.emailing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import fr.iolabs.leaf.common.errors.InternalServerErrorException;

@Service
public class LeafMailgunEmailService {

    @Value("${leaf.emailing.mailgun.api.url}")
    String mailGunAPIUrl;

    @Value("${leaf.emailing.mailgun.api.key}")
    String mailGunAPIKey;

    @Value("${leaf.emailing.mailgun.email.from}")
    String mailGunAPIFrom;

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
