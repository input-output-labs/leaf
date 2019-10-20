package fr.iolabs.leaf.messenger.rooms;

import fr.iolabs.leaf.messenger.rooms.messages.Message;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;

public class Room implements Ownable {
    @Id
    private String id;
    private String ownerId;
    private LocalDateTime createdAt;

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
