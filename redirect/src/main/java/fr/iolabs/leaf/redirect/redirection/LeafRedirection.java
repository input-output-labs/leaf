package fr.iolabs.leaf.redirect.redirection;

import org.springframework.data.annotation.Id;

public class LeafRedirection {
    @Id
    private long id;
    
    private String creationBatchId;
    
    private String redirectUrl;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

	public String getCreationBatchId() {
		return creationBatchId;
	}

	public void setCreationBatchId(String creationBatchId) {
		this.creationBatchId = creationBatchId;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}
}
