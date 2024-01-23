package fr.iolabs.leaf.files;

public class FileSaveOptions {
	private String publicfolder;

	public String getPublicfolder() {
		return publicfolder;
	}

	public FileSaveOptions publicfolder(String publicfolder) {
		this.publicfolder = publicfolder;
		return this;
	}
}
