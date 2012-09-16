package tk.eclipse.plugin.jspeditor.editors;

import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.ISourceViewer;

import tk.eclipse.plugin.htmleditor.ColorProvider;
import tk.eclipse.plugin.htmleditor.HTMLPlugin;
import tk.eclipse.plugin.htmleditor.assist.HTMLAssistProcessor;
import tk.eclipse.plugin.htmleditor.editors.HTMLConfiguration;
import tk.eclipse.plugin.htmleditor.editors.HTMLPartitionScanner;

/**
 * SourceViewerConfiguration for the JSP editor.
 */
public class JSPConfiguration extends HTMLConfiguration {
	
	private JSPScriptletScanner scriptletScanner = null;
	private JSPDirectiveScanner directiveScanner = null;
	private IContentAssistant assistant = null;
	
	/**
	 * @param colorProvider
	 */
	public JSPConfiguration(ColorProvider colorProvider) {
		super(colorProvider);
	}
	
	protected HTMLAssistProcessor createAssistProcessor() {
		return new JSPAssistProcessor();
	}
	
	protected RuleBasedScanner getScriptScanner() {
		if (scriptletScanner == null) {
			scriptletScanner = new JSPScriptletScanner(getColorProvider());
			scriptletScanner.setDefaultReturnToken(
					getColorProvider().getToken(HTMLPlugin.PREF_COLOR_FG));
		}
		return scriptletScanner;
	}
	
	protected RuleBasedScanner getDirectiveScanner() {
		if (directiveScanner == null) {
			directiveScanner = new JSPDirectiveScanner(getColorProvider());
			directiveScanner.setDefaultReturnToken(
					getColorProvider().getToken(HTMLPlugin.PREF_COLOR_TAG));
		}
		return directiveScanner;
	}
	
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		if(assistant==null){
			assistant = super.getContentAssistant(sourceViewer);
			JSPDirectiveAssistProcessor processor = new JSPDirectiveAssistProcessor();
			((ContentAssistant)assistant).setContentAssistProcessor(processor,HTMLPartitionScanner.HTML_DIRECTIVE);
		}
		return assistant;
	}

}
