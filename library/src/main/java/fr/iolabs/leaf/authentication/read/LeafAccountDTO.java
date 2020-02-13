package fr.iolabs.leaf.authentication.read;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import fr.iolabs.leaf.authentication.model.LeafAccount;

public class LeafAccountDTO {
    private String id;
    private String email;
    private String username;
    private String avatarUrl;
    private Set<PrivateTokenDTO> privateTokens;
    private boolean admin;
    private Map<String, Object> modules;
    
    public static LeafAccountDTO from(LeafAccount account) {
		LeafAccountDTO dto = new LeafAccountDTO();
		dto.setId(account.getId());
		dto.setEmail(account.getEmail());
		dto.setUsername(account.getUsername());
		dto.setAvatarUrl(account.getAvatarUrl());
		dto.setPrivateTokens(account.getPrivateTokens().stream().map(PrivateTokenDTO::from).collect(Collectors.toSet()));
		dto.setModules(account.getModules());
		dto.setAdmin(account.isAdmin());
		return dto;
    }
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getAvatarUrl() {
		return avatarUrl;
	}
	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}
	public Set<PrivateTokenDTO> getPrivateTokens() {
		return privateTokens;
	}
	public void setPrivateTokens(Set<PrivateTokenDTO> privateTokens) {
		this.privateTokens = privateTokens;
	}

	public Map<String, Object> getModules() {
		return modules;
	}

	public void setModules(Map<String, Object> modules) {
		this.modules = modules;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
}
