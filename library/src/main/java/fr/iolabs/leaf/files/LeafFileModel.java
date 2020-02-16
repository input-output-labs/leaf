package fr.iolabs.leaf.files;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class LeafFileModel {
    private String id;
    private LocalDateTime uploadDateTime;
    private byte[] file;
    private String uploaderAccountId;
    private String contentType;
    private String url;

    public static LeafFileModel from(byte[] fileBytes, String contentType, String uploaderAccountId) {
        LeafFileModel file = new LeafFileModel();
        file.uploadDateTime = LocalDateTime.now();
        file.file = fileBytes;
        file.uploaderAccountId = uploaderAccountId;
        file.contentType = contentType;
        return file;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime getUploadDateTime() {
        return uploadDateTime;
    }

    public void setUploadDateTime(LocalDateTime uploadDateTime) {
        this.uploadDateTime = uploadDateTime;
    }

    @JsonIgnore
    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public String getUploaderAccountId() {
        return uploaderAccountId;
    }

    public void setUploaderAccountId(String uploaderAccountId) {
        this.uploaderAccountId = uploaderAccountId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url.replace("http://", "https://");
	}
}
