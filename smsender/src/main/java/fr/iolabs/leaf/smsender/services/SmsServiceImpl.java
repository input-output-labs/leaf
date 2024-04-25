package fr.iolabs.leaf.smsender.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsServiceImpl implements SmsService{

    @Value("${leaf.smsender.twilio.api.accountSid}")
    private String twilioAccountSid;

    @Value("${leaf.smsender.twilio.api.authToken}")
    private String twilioAuthToken;

    @Value("${leaf.smsender.twilio.api.phoneNumber}")
    private String twilioPhoneNumber;

    public void sendSMS(String to, String message) {
        Twilio.init(twilioAccountSid, twilioAuthToken);
        Message.creator(new PhoneNumber(to), new PhoneNumber(twilioPhoneNumber), message).create();
    }
}
