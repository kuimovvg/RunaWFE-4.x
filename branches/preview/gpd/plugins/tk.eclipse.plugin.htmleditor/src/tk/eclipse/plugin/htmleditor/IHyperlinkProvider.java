package tk.eclipse.plugin.htmleditor;

import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;

import org.eclipse.core.resources.IFile;

import tk.eclipse.plugin.htmleditor.editors.HTMLHyperlinkInfo;

/**
 * This extends the hyperlink feature of the HTML editor.
 * 
 * @author Naoki Takezoe
 */
public interface IHyperlinkProvider {
	
	/**
	 * This method returns a target object of hyperlink.
	 * If this provider doesn't support specified arguments, returns null.
	 * 
	 * @param file      IFile
	 * @param doc       a document object of FuzzyXML
	 * @param element   an element that are calet position
	 * @param attrName  an attribute name that are calet position
	 * @param attrValue an attribute value that are calet position
	 * @return 
	 *   <ul>
	 *     <li>IFile</li>
	 *     <li>IJavaElement</li>
	 *     <li>null</li>
	 *   </ul>
	 */
	public HTMLHyperlinkInfo getHyperlinkInfo(IFile file,FuzzyXMLDocument doc,
			FuzzyXMLElement element,String attrName,String attrValue,int offset);
	
}
