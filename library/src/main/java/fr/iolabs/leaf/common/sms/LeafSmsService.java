package fr.iolabs.leaf.common.sms;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Map;


/**
 * Service for sending sms using Twilio
 */
@Service
public class LeafSmsService {

    @Autowired
    private LeafSmsTemplateService smsTemplateService;

    @Value("${leaf.sms.twilio.api.accountSid}")
    private String twilioAccountSid;

    @Value("${leaf.sms.twilio.api.authToken}")
    private String twilioAuthToken;

    @Value("${leaf.sms.twilio.api.phoneNumber}")
    private String twilioPhoneNumber;

    /**
     * Sends an SMS message using a template using thymeleaf template generator
     * @param phoneNumber      user's phone number
     * @param templateId   id of the sms template to be sent
     * @param templateData data that will be added to the template
     * @param language     language of the message
     */
    public void sendSmsWithTemplate(String phoneNumber, String templateId, Map<String, Object> templateData, String language) {
        String result = this.smsTemplateService.renderSmsMessage(templateId, language, templateData);
        this.sendSms(phoneNumber, result);
    }

    /**
     * Sends sms to a specified number
     *
     * @param phoneNumber of the recipient
     * @param message content of the SMS message.
     */
    private void sendSms(String phoneNumber, String message) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new IllegalArgumentException("Phone number missing");
        }
        Twilio.init(twilioAccountSid, twilioAuthToken);
        Message.creator(new PhoneNumber(phoneNumber), new PhoneNumber(twilioPhoneNumber), message).create();
    }
}
