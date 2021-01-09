package fr.iolabs.leaf.authentication.read;

import java.util.List;
import java.util.stream.Collectors;

import fr.iolabs.leaf.authentication.model.LeafUser;

public class LeafUserDTO {
    private String id;
    private String username;
    private String avatarUrl;
    
    public static LeafUserDTO from(LeafUser user) {
		LeafUserDTO dto = new LeafUserDTO();
		dto.setId(user.getId());
		dto.setUsername(user.getUsername());
		dto.setAvatarUrl(user.getAvatarUrl());
		return dto;
    }
    
    public static List<LeafUserDTO> fromAll(List<? extends LeafUser> list) {
    	return list.stream().map(LeafUserDTO::from).collect(Collectors.toList());
    }
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
}
