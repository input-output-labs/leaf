package fr.iolabs.leaf.common.sms.model;
import java.util.Map;

public class SmsRequest {
    private String language;
    private Map<String, Object> payload;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}