package fr.iolabs.leaf.common.sms.model;

import fr.iolabs.leaf.authentication.model.ResourceMetadata;
import org.springframework.data.annotation.Id;

import java.util.Map;

/**
 * Represents an SMS template that will be stored in the database
 */
public class LeafSmsTemplate {
    @Id
    private String id;
    private Map<String, String> contentPerLanguage;
    private ResourceMetadata metadata;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getContentPerLanguage() {
        return contentPerLanguage;
    }

    public void setContentPerLanguage(Map<String, String> contentPerLanguage) {
        this.contentPerLanguage = contentPerLanguage;
    }

    public ResourceMetadata getMetadata() {
        if (this.metadata == null) {
            this.metadata = ResourceMetadata.create();
        }
        return this.metadata;
    }

    public void setMetadata(ResourceMetadata metadata) {
        this.metadata = metadata;
    }
}