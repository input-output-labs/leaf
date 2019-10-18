package fr.iolabs.leaf.authentication.read;

import java.time.LocalDate;

import fr.iolabs.leaf.authentication.model.PrivateToken;

public class PrivateTokenDTO {
    private String name;
    private LocalDate created;
    private LocalDate expiration;
    private String accountId;
    
    public static PrivateTokenDTO from(PrivateToken token) {
    	PrivateTokenDTO dto = new PrivateTokenDTO();
    	dto.name = token.getName();
    	dto.created = token.getCreated();
    	dto.expiration = token.getExpiration();
    	dto.accountId = token.getAccountId();
    	return dto;
    }

	public PrivateToken toPrivateToken() {
		PrivateToken dto = new PrivateToken();
    	dto.setName(this.getName());
    	dto.setCreated(this.getCreated());
    	dto.setExpiration(this.getExpiration());
    	dto.setAccountId(this.getAccountId());
    	return dto;
	} 
    
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public LocalDate getCreated() {
		return created;
	}
	public void setCreated(LocalDate created) {
		this.created = created;
	}
	public LocalDate getExpiration() {
		return expiration;
	}
	public void setExpiration(LocalDate expiration) {
		this.expiration = expiration;
	}
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}  
}
