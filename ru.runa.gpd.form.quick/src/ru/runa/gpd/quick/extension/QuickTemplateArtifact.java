package ru.runa.gpd.quick.extension;

import ru.runa.gpd.extension.Artifact;

public class QuickTemplateArtifact extends Artifact {
	private String fileName;
	public QuickTemplateArtifact() {
		
	}
	public QuickTemplateArtifact(boolean enabled, String name, String label, String fileName) {
        super(enabled, name, label);
        setFileName(fileName);
    }
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	
}
