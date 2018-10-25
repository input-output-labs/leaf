package fr.iolabs.leaf.errors;

public class WebException extends RuntimeException {
	private static final long serialVersionUID = 6295151762267787645L;

	private int code;
	private String name;
	
	protected WebException(int code, String name) {
		this.code = code;
		this.name = name;
	}

	public int getCode() {
		return code;
	}

	public String getName() {
		return name;
	}
}
