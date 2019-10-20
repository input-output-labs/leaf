package fr.iolabs.leaf.messenger.rooms.messages;

import java.time.LocalDateTime;

import fr.iolabs.leaf.messenger.rooms.Ownable;

public class Message implements Ownable {
    private String id;
    private String ownerId;
    private LocalDateTime createdAt;

    private String content;
    private String attachedFileUrl;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAttachedFileUrl() {
        return attachedFileUrl;
    }

    public void setAttachedFileUrl(String attachedFileUrl) {
        this.attachedFileUrl = attachedFileUrl;
    }
}
