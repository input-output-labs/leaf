package fr.iolabs.leaf.payment.services;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import fr.iolabs.leaf.authentication.model.ResourceMetadata;
import fr.iolabs.leaf.payment.plan.config.PlanAttachment;

@Document(collection = "leafServices")
public class LeafService {
    @Id
    private String id;
    
    private PlanAttachment attachmentType;
    private String attachedTo;
    private String key;
    private String stripeProductId;
    private String icon;
    private long unitPrice; // in cents
    private int quantity;
    private boolean automaticQuantities;
    private boolean useSubscription;
    private ResourceMetadata metadata;

    public LeafService() {
        this.metadata = ResourceMetadata.create();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PlanAttachment getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(PlanAttachment attachmentType) {
        this.attachmentType = attachmentType;
    }

    public String getAttachedTo() {
        return attachedTo;
    }

    public void setAttachedTo(String attachedTo) {
        this.attachedTo = attachedTo;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public long getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(long unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public ResourceMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ResourceMetadata metadata) {
        this.metadata = metadata;
    }

    public boolean isAutomaticQuantities() {
        return automaticQuantities;
    }

    public void setAutomaticQuantities(boolean automaticQuantities) {
        this.automaticQuantities = automaticQuantities;
    }

    public boolean isUseSubscription() {
        return useSubscription;
    }

    public void setUseSubscription(boolean useSubscription) {
        this.useSubscription = useSubscription;
    }

	public String getStripeProductId() {
		return stripeProductId;
	}

	public void setStripeProductId(String stripeProductId) {
		this.stripeProductId = stripeProductId;
	}
}
