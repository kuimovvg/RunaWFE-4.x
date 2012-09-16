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
package ru.runa.commons.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import ru.runa.InternalApplicationException;

/**
 * Created on 26.07.2005
 * 
 */
public class XMLHelper {
    private XMLHelper() {
    }

    public static Document getDocument(String path, EntityResolver entityResolver, ErrorHandler errorHandler) throws SAXException, IOException {
        DocumentBuilder documentBuilder = getDocumentBuilder(entityResolver, errorHandler);
        return documentBuilder.parse(path);
    }

    public static Document getDocument(String path, EntityResolver entityResolver) throws SAXException, IOException {
        return getDocument(path, entityResolver, SimpleErrorHandler.getInstance());
    }

    public static Document getDocument(InputStream inputStream, EntityResolver entityResolver, ErrorHandler errorHandler) throws SAXException,
            IOException {
        DocumentBuilder documentBuilder = getDocumentBuilder(entityResolver, errorHandler);
        return documentBuilder.parse(inputStream);
    }

    public static Document getDocument(InputStream inputStream, EntityResolver entityResolver) throws SAXException, IOException {
        return getDocument(inputStream, entityResolver, SimpleErrorHandler.getInstance());
    }

    public static Document getDocument(InputStream inputStream) throws SAXException, IOException {
        return getDocument(inputStream, ClasspathEntityResolver.getInstance(), SimpleErrorHandler.getInstance());
    }

    public static Document getDocumentWithoutValidation(InputStream inputStream) throws SAXException, IOException {
        return getDocument(inputStream, null, SimpleErrorHandler.getInstance());
    }

    public static Document newDocument(EntityResolver entityResolver, ErrorHandler errorHandler) throws SAXException, IOException {
        DocumentBuilder documentBuilder = getDocumentBuilder(entityResolver, errorHandler);
        return documentBuilder.newDocument();
    }

    private static DocumentBuilder getDocumentBuilder(EntityResolver entityResolver, ErrorHandler errorHandler) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            if (entityResolver != null) {
                factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
                factory.setValidating(true);
            }
            factory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            documentBuilder.setEntityResolver(entityResolver);
            documentBuilder.setErrorHandler(errorHandler);
            return documentBuilder;
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }
}
