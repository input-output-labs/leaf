package fr.iolabs.leaf.common.errors;

public class WebException extends RuntimeException {
	private static final long serialVersionUID = 6295151762267787645L;

	private final int code;
	private final String name;
	
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
