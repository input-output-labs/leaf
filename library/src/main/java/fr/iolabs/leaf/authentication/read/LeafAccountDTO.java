package fr.iolabs.leaf.authentication.read;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import fr.iolabs.leaf.authentication.model.LeafAccount;

public class LeafAccountDTO extends LeafUserDTO {
    private String email;
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

    public static List<LeafAccountDTO> from(List<LeafAccount> accounts) {
    	return accounts.stream().map(LeafAccountDTO::from).collect(Collectors.toList());
    }
    
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
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
