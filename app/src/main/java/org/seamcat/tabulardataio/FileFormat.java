package org.seamcat.tabulardataio;

public class FileFormat {
	private String name;
	private String extension;
	
	public FileFormat(String name, String extension) {
		this.name = name;
		this.extension = extension;
	}

	public String getName() {
		return name;
	}

	public String getExtension() {
		return extension;
	}		
}
