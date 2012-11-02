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
package ru.runa.common.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.MimeUtility;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import ru.runa.wfe.InternalApplicationException;

import com.google.common.base.Charsets;

public class HTMLUtils {

    private HTMLUtils() {
    }

    public static byte[] writeHtml(Document document) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "html");
            transformer.setOutputProperty(OutputKeys.ENCODING, Charsets.UTF_8.name());
            transformer.transform(new DOMSource(document), new StreamResult(outputStream));
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

    public static Document readHtml(byte[] htmlBytes) {
        try {
            DOMParser parser = new DOMParser();
            InputSource inputSource = new InputSource(new ByteArrayInputStream(htmlBytes));
            inputSource.setEncoding(Charsets.UTF_8.name());
            parser.parse(inputSource);
            return parser.getDocument();
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

    public static List<String> findImages(byte[] htmlBytes) {
        List<String> list = new ArrayList<String>();
        Document document = readHtml(htmlBytes);

        NodeList imgElements = document.getElementsByTagName("img");
        for (int i = 0; i < imgElements.getLength(); i++) {
            Element imgElement = (Element) imgElements.item(i);
            String path = imgElement.getAttribute("src");
            list.add(path);
        }
        return list;
    }

    public static String encodeFileName(String fileName, String userAgent) {
        try {
            if (userAgent != null) {
                if (userAgent.indexOf("MSIE") != -1) {
                    // IE
                    fileName = URLEncoder.encode(fileName, Charsets.UTF_8.name());
                    fileName = fileName.replaceAll("\\+", " ");
                } else {
                    fileName = MimeUtility.encodeText(fileName, Charsets.UTF_8.name(), "B");
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new InternalApplicationException(e);
        }
        return fileName;
    }

}
