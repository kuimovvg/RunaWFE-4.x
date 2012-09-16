

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import javax.xml.transform.stream.StreamSource;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;

import java.io.*;

import java.util.UUID;


public class MSIComponentsTuner {
	
	static String inputFile = "D:\\Work\\Java\\ztmp.xml";
	static String lastFile = "D:\\Work\\Java\\zlast.xml";
	static String outputFile = "D:\\Work\\Java\\ztmp2.xml";
	
	public static void main(String[] args){
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		if(args.length != 4)
			throw new IllegalArgumentException("\nExpected 3 arguments: \n1) File, genarated by tallow.exe\n2) File, which used by prev. version of MSI\n3) File to store result of GUID replacement"); 

		inputFile=args[0];
		lastFile=args[1];
		outputFile=args[2];
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			//parse using builder to get DOM representation of the XML file's
			Document processingDocument = db.parse(inputFile);
			Document etalonDocument = null;
			try{
				// This may not exists
				etalonDocument = db.parse(lastFile);
			}catch(IOException ioe) {
				// Nothing to do - it's normal, just generate new GUID's
			}
			
			// Generate GUID's instead of "PUT-GUID-HERE" (with relation to history)
			processDomInsertGUID(
					processingDocument.getDocumentElement(), 
					etalonDocument == null ? null : etalonDocument.getDocumentElement());
			
			// Save result
			DOMSource source = new DOMSource(processingDocument);
            StreamResult result = new StreamResult(new FileOutputStream(outputFile));
            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer();
            transformer.setOutputProperty("encoding", args[3]);
            transformer.transform(source, result);	
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	static private Document applyXSLT(Document sourceDocument, String fileXSLT)
		throws TransformerConfigurationException, TransformerException{
		DOMSource source = new DOMSource(sourceDocument);
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = fileXSLT == null ? transFactory.newTransformer() : transFactory.newTransformer(new StreamSource(new File(fileXSLT)));
        DOMResult domRes = new DOMResult();
        transformer.transform(source, domRes);
        Document retVal = (Document)domRes.getNode();
        retVal.setXmlStandalone(true);
        return retVal;
		
	}

	static private boolean checkAttributesEquals(Attr attr1, Attr attr2){
		if(((attr1 == null) ^ (attr2 == null))) 
			return false;
		if( attr1 != null)
			if(! attr1.getValue().equals(attr2.getValue()))
				return false;

		return true;
	}
	
	static private boolean checkDirectoryElementsEquals(Element element1, Element element2){
		if(element1.getLocalName() != element2.getLocalName()){
			throw new InternalError("Dom parsing is incorrect - revision is required.");
		}
		
		if(!checkAttributesEquals(element1.getAttributeNode("LongName"), element2.getAttributeNode("LongName")))
			return false;
		if(!checkAttributesEquals(element1.getAttributeNode("Name"), element2.getAttributeNode("Name")))
			return false;
		
		return true;
	}

	static private void processDirectorySubElement(Element elementDirectory, Element etalonContainer){
		// Will search same directory in etalonContainer and call recursive
		if(etalonContainer == null){
			processDomInsertGUID(elementDirectory, null);
			return;
		}
		
		NodeList etalonCandidates = etalonContainer.getChildNodes();
		Element etalonElement = null;
		for(int eId = 0; eId < etalonCandidates.getLength(); ++eId){
			Node nodeEtalon = etalonCandidates.item(eId);
			if (!(nodeEtalon instanceof Element)) 
				continue;
			Element etalonCandid = (Element)nodeEtalon;
			
			if(!etalonCandid.getNodeName().equals("Directory")) 
				continue;
			if(checkDirectoryElementsEquals(elementDirectory, etalonCandid)){
				etalonElement = etalonCandid;
				break;
			}
		}
		processDomInsertGUID(elementDirectory, etalonElement);
	}

	static private void processComponentSubElement(Element elementComponent, Element etalonContainer){
		if(etalonContainer == null){
			elementComponent.getAttributeNode("Guid").setValue(UUID.randomUUID().toString().toUpperCase());
			return; 
		}
		
		NodeList etalonCandidates = etalonContainer.getChildNodes();
		boolean isGUIDSet = false;
		for(int eId = 0; eId < etalonCandidates.getLength(); ++eId){
			Node nodeEtalon = etalonCandidates.item(eId);
			if (! (nodeEtalon instanceof Element)) continue;
			Element etalonCandid = (Element)nodeEtalon;
			
			if(!etalonCandid.getNodeName().equals("Component")) continue;

			elementComponent.getAttributeNode("Guid").setValue(etalonCandid.getAttributeNode("Guid").getValue());
			isGUIDSet = true;
			break;
		}
		if(!isGUIDSet){
			elementComponent.getAttributeNode("Guid").setValue(UUID.randomUUID().toString().toUpperCase());
		}
	}

	static private void processUnknownSubElement(Element elementUnknown, Element etalonContainer){
		if(etalonContainer == null){
			processDomInsertGUID(elementUnknown, null);
			return; 
		}
		
		NodeList etalonCandidates = etalonContainer.getChildNodes();
		Element etalonElement = null;

		for(int eId = 0; eId < etalonCandidates.getLength(); ++eId){
			Node nodeEtalon = etalonCandidates.item(eId);
			if (! (nodeEtalon instanceof Element)) continue;
			Element etalonCandid = (Element)nodeEtalon;

			if(elementUnknown.getNodeName().equals(etalonCandid.getNodeName())){
				etalonElement = etalonCandid;
				break;
			}
		}
		processDomInsertGUID(elementUnknown, etalonElement);
	}

	static private void processDomInsertGUID(Element processingElement, Element etalonElement){
		if(etalonElement != null && processingElement.getNodeName() != etalonElement.getNodeName()){
			throw new InternalError("Dom parsing is incorrect - revision is required.");
		}
		NodeList processingNodes = processingElement.getChildNodes();
		for(int pId = 0; pId < processingNodes.getLength(); ++pId){
			Node node = processingNodes.item(pId);
			if (! (node instanceof Element)) continue;
			Element subProcessingElement = (Element)node;
			
			if(subProcessingElement.getNodeName().equals("Directory")){
				processDirectorySubElement(subProcessingElement, etalonElement);
				continue;
			}

			if(subProcessingElement.getNodeName().equals("Component")){
				processComponentSubElement(subProcessingElement, etalonElement);
				continue;
			}
			
			processUnknownSubElement(subProcessingElement, etalonElement);			
		}
	}
}
