package fr.iolabs.leaf.smsender.services;

public interface SmsService {
    /**
     * Sends sms to a specified number
     *
     * @param to phone number of the recipient.
     * @param message content of the SMS message.
     */
    void sendSMS(String to, String message);
}