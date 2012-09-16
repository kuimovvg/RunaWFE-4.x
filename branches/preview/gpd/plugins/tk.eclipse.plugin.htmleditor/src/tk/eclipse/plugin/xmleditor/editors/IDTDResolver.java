package tk.eclipse.plugin.xmleditor.editors;

import java.io.InputStream;

public interface IDTDResolver {
	
	public InputStream getInputStream(String uri);
	
}
