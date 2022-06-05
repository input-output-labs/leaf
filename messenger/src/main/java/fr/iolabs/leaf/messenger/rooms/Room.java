package fr.iolabs.leaf.messenger.rooms;

import fr.iolabs.leaf.authentication.model.ResourceMetadata;
import fr.iolabs.leaf.messenger.rooms.messages.Message;
import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.Set;

public class Room implements Ownable {
    @Id
    private String id;
    private String ownerId;
    private Set<String> members;
    private ResourceMetadata metadata;

    private String name;
    private List<Message> messages;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Set<String> getMembers() {
		return members;
	}

	public void setMembers(Set<String> members) {
		this.members = members;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
