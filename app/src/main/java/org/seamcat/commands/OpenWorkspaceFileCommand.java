package org.seamcat.commands;

import java.io.File;

public class OpenWorkspaceFileCommand {

	private File file;

	public OpenWorkspaceFileCommand( File file ) {
		this.file = file;
	}
	
	public File getFile() {
		return file;
	}
}
