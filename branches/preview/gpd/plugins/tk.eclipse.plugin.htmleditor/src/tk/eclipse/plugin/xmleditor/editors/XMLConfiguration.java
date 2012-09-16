package tk.eclipse.plugin.xmleditor.editors;

import tk.eclipse.plugin.htmleditor.ColorProvider;
import tk.eclipse.plugin.htmleditor.assist.HTMLAssistProcessor;
import tk.eclipse.plugin.htmleditor.editors.HTMLConfiguration;

public class XMLConfiguration extends HTMLConfiguration {
	
//	private XMLAssistProcessor assistProcessor;
	
	public XMLConfiguration(ColorProvider colorProvider) {
		super(colorProvider);
	}
	
	protected HTMLAssistProcessor createAssistProcessor() {
		return new XMLAssistProcessor();
	}
	
}
