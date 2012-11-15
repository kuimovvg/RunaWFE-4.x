package ru.runa.gpd.formeditor.vartag;

import java.io.IOException;
import java.io.InputStream;

import org.osgi.framework.Bundle;

import ru.runa.gpd.formeditor.WYSIWYGPlugin;

public class VarTagInfo {
    private final Bundle bundle;
    private final String imagePath;

    public final String displayName;
	public final String javaType;
	public final int width;
	public final int height;
	public final boolean inputTag;
	
	public VarTagInfo(Bundle bundle, String className, String displayName, int width, int height, String imagePath, boolean inputTag) {
	    this.bundle = bundle;
		this.displayName = displayName;
		this.javaType = className;
		this.width = width;
		this.height = height;
		this.imagePath = imagePath;
		this.inputTag = inputTag;
	}

    public boolean hasImage() {
        return imagePath != null;
    }
    
    public InputStream openImageStream() throws IOException {
        return WYSIWYGPlugin.loadTagImage(bundle, imagePath);
    }

}
