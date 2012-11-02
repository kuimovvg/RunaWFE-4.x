/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.service.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ru.runa.service.delegate.DelegateFactory;
import ru.runa.service.wf.AdminScriptService;
import ru.runa.wfe.commons.IOCommons;
import ru.runa.wfe.commons.xml.PathEntityResolver;
import ru.runa.wfe.commons.xml.SimpleErrorHandler;
import ru.runa.wfe.commons.xml.XMLHelper;

import com.google.common.io.Files;

/**
 * Created on 12.12.2005
 * 
 */
public class WfeScriptClient {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: AdminScriptRunner <scriptpath> <username> <password>");
            System.out.println("Example: AdminScriptRunner /home/foo/wfescript.xml foo $eCreTw0rd");
            System.exit(-1);
        }
        File file = new File(args[0]);
        if (!file.exists()) {
            System.out.println("Config file " + args[0] + " does not exist");
            System.exit(-1);
        }
        try {
            byte[] scriptBytes = Files.toByteArray(file);
            run(args[1], args[2], scriptBytes);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    private static void run(String login, String password, byte[] scriptBytes) throws Exception {
        AdminScriptService delegate = DelegateFactory.getAdminScriptService();
        InputStream scriptInputStream = new ByteArrayInputStream(scriptBytes);
        Document allDocument = XMLHelper.getDocument(scriptInputStream, PATH_ENTITY_RESOLVER);

        NodeList transactionScopeNodeList = allDocument.getElementsByTagName("transactionScope");
        String defaultTransactionScope = allDocument.getDocumentElement().getAttribute("defaultTransactionScope");
        if (transactionScopeNodeList.getLength() == 0 && "all".equals(defaultTransactionScope)) {
            byte[][] processDefinitionsBytes = readProcessDefinitionsToByteArrays(allDocument);
            delegate.run(login, password, scriptBytes, processDefinitionsBytes);
        } else {
            if (transactionScopeNodeList.getLength() > 0) {
                System.out.println("multiple docs [by <transactionScope>]: " + transactionScopeNodeList.getLength());
                for (int i = 0; i < transactionScopeNodeList.getLength(); i++) {
                    Node transactionScopeElement = transactionScopeNodeList.item(i);
                    Document document = createScriptDocument();
                    NodeList chidren = transactionScopeElement.getChildNodes();
                    for (int j = 0; j < chidren.getLength(); j++) {
                        Node child = chidren.item(j);
                        if (child.getNodeType() == Node.ELEMENT_NODE) {
                            document.getDocumentElement().appendChild(document.importNode(child, true));
                        }
                    }
                    byte[] bs = writeDocument(document);
                    byte[][] processDefinitionsBytes = readProcessDefinitionsToByteArrays(document);
                    try {
                        delegate.run(login, password, bs, processDefinitionsBytes);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            } else {
                NodeList allChildrenNodeList = allDocument.getDocumentElement().getChildNodes();
                System.out.println("multiple docs [by defaultTransactionScope]: " + allChildrenNodeList.getLength());
                for (int i = 0; i < allChildrenNodeList.getLength(); i++) {
                    Node child = allChildrenNodeList.item(i);
                    if (child.getNodeType() == Node.ELEMENT_NODE) {
                        Document document = createScriptDocument();
                        document.getDocumentElement().appendChild(document.importNode(child, true));
                        byte[] bs = writeDocument(document);
                        byte[][] processDefinitionsBytes = readProcessDefinitionsToByteArrays(document);
                        try {
                            delegate.run(login, password, bs, processDefinitionsBytes);
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private static Document createScriptDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
        factory.setValidating(true);
        factory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        documentBuilder.setEntityResolver(PATH_ENTITY_RESOLVER);
        documentBuilder.setErrorHandler(SimpleErrorHandler.getInstance());

        Document document = documentBuilder.newDocument();
        Element root = document.createElement("workflowScript");
        root.setAttribute("xmlns", "http://runa.ru/xml");
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.setAttribute("xsi:schemaLocation", "http://runa.ru/xml workflowScript.xsd");
        document.appendChild(root);
        return document;
    }

    private static byte[] writeDocument(Document document) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        DOMSource source = new DOMSource(document);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(baos);
        transformer.transform(source, result);
        System.out.println(new String(baos.toByteArray()));
        return baos.toByteArray();
    }

    private static byte[][] readProcessDefinitionsToByteArrays(Document document) throws ParserConfigurationException, SAXException, IOException {
        String[] fileNames = readProcessDefinitionFileNames(document);
        byte[][] processDefinitionsBytes = new byte[fileNames.length][];
        for (int i = 0; i < fileNames.length; i++) {
            File processFile = new File(fileNames[i]);
            if (processFile.isFile()) {
                processDefinitionsBytes[i] = Files.toByteArray(new File(fileNames[i]));
            } else {
                processDefinitionsBytes[i] = IOCommons.jarToBytesArray(processFile);
            }
        }
        return processDefinitionsBytes;
    }

    private static final String DEPLOY_PROCESS_DEFINITION_TAG_NAME = "deployProcessDefinition";

    private static final String FILE_ATTRIBUTE_NAME = "file";

    private static final String XSD_PATH = "/workflowScript.xsd";

    private static final PathEntityResolver PATH_ENTITY_RESOLVER = new PathEntityResolver(XSD_PATH);

    private static String[] readProcessDefinitionFileNames(Document document) throws ParserConfigurationException, SAXException, IOException {
        NodeList deployProcessDefinitionNodeList = document.getElementsByTagName(DEPLOY_PROCESS_DEFINITION_TAG_NAME);
        String[] fileNames = new String[deployProcessDefinitionNodeList.getLength()];
        for (int i = 0; i < fileNames.length; i++) {
            fileNames[i] = ((Element) deployProcessDefinitionNodeList.item(i)).getAttribute(FILE_ATTRIBUTE_NAME);
        }
        return fileNames;
    }
}
