package tk.eclipse.plugin.jspeditor.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import tk.eclipse.plugin.htmleditor.assist.AttributeInfo;
import tk.eclipse.plugin.htmleditor.assist.TagInfo;

public class TLDSAXHandler extends DefaultHandler {
	
	private int mode = 0;
	private String prevTag = null;
	private boolean hasBody = true;
	private ArrayList attributes = new ArrayList();
	private HashMap attrRequiredMap = new HashMap();
	private HashMap attrDescMap = new HashMap();
	private String uri = null;
	
	private StringBuffer tagName = new StringBuffer();
	private StringBuffer attrName = new StringBuffer();
	private StringBuffer tagDesc = new StringBuffer();
	private StringBuffer attrDesc = new StringBuffer();
	
	private String prefix = "";
	private ArrayList result = new ArrayList();
	
	/**
	 * Use specified prefix.
	 * 
	 * @param prefix
	 */
	public TLDSAXHandler(String prefix){
		this.prefix = prefix;
	}
	
	/**
	 * Use tld's shortname as prefix.
	 */
	public TLDSAXHandler(){
		this.prefix = null;
	}
	
	public String getUri(){
		return uri;
	}
	
	public List getResult(){
		return result;
	}
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if(qName.equals("tag")){
			tagDesc.setLength(0);
			mode = 1;
		} else if(qName.equals("attribute")){
			attrDesc.setLength(0);
			mode = 2;
		}
		prevTag = qName;
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(qName.equals("tag")){
			TagInfo info = new TagInfo(tagName.toString(),hasBody);
			for(int i=0;i<attributes.size();i++){
				String attrName = (String)attributes.get(i);
				boolean required = false;
				if(this.attrRequiredMap.get(attrName)!=null){
					required = true;
				}
				AttributeInfo attrInfo = new AttributeInfo(attrName,true,AttributeInfo.NONE,required);
				if(attrDescMap.get(attrName)!=null){
					attrInfo.setDescription(wrap((String)attrDescMap.get(attrName)));
				}
				info.addAttributeInfo(attrInfo);
			}
			if(tagDesc.length() > 0){
				info.setDescription(wrap(tagDesc.toString()));
			}
			result.add(info);
			mode = 0;
			prevTag = null;
			hasBody = true;
			attrRequiredMap.clear();
			attrDescMap.clear();
			tagName.setLength(0);
			tagDesc.setLength(0);
			attributes.clear();
		} else if(qName.equals("name") && mode==2){
			attributes.add(attrName.toString());
			attrName.setLength(0);
		} else if(qName.equals("description") && mode==2){
			if(attrDesc.length() > 0){
				attrDescMap.put(attributes.get(attributes.size()-1), attrDesc.toString());
				attrDesc.setLength(0);
			}
		}
	}
	
	public void characters(char[] ch, int start, int length) throws SAXException {
		StringBuffer sb = new StringBuffer();
		for(int i=start;i<start+length;i++){
			sb.append(ch[i]);
		}
		String value = sb.toString().trim();
		if(!value.equals("")){
			if(prevTag.equals("name")){
				if(mode==1){
					if(tagName.length()==0){
						tagName.append(prefix+":");
					}
					tagName.append(value);
				} else {
					attrName.append(value);
				}
			} else if(prevTag.equals("bodycontent")){
				if(value.equals("empty")){
					hasBody = false;
				} else {
					hasBody = true;
				}
			} else if(prevTag.equals("required")){
				if(value.equals("true")){
					attrRequiredMap.put(attributes.get(attributes.size()-1),"true");
				}
			} else if(prevTag.equals("uri")){
				uri = value;
			} else if(prefix==null && (prevTag.equals("shortname") || prevTag.equals("short-name"))){
				prefix = value;
			} else if(prevTag.equals("description")){
				if(mode==1){
					tagDesc.append(value);
				} else if(mode==2){
					attrDesc.append(value);
				}
			}
		}
	}
	
	private static String wrap(String text){
		StringBuffer sb = new StringBuffer();
		int word = 0;
		for(int i=0;i<text.length();i++){
			char c = text.charAt(i);
			if(word > 40){
				if(c==' ' || c== '\t'){
					sb.append('\n');
					word = 0;
					continue;
				}
			}
			sb.append(c);
			word++;
		}
		return sb.toString();
	}
}
