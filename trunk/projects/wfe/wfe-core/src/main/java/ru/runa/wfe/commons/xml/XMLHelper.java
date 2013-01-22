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
package ru.runa.wfe.commons.xml;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;

import ru.runa.wfe.InternalApplicationException;

/**
 * Unifies XML operations.
 * 
 * @deprecated use {@link XmlUtils}.
 */
public class XMLHelper {

    public static Document getDocument(InputStream inputStream, EntityResolver entityResolver, ErrorHandler errorHandler) {
        DocumentBuilder documentBuilder = getDocumentBuilder(entityResolver, errorHandler);
        try {
            return documentBuilder.parse(inputStream);
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

    public static Document getDocument(InputStream inputStream, EntityResolver entityResolver) {
        return getDocument(inputStream, entityResolver, SimpleErrorHandler.getInstance());
    }

    public static Document newDocument(EntityResolver entityResolver, ErrorHandler errorHandler) {
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
