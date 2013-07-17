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

import javax.mail.internet.MimeUtility;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.ecs.html.Div;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;

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
            throw Throwables.propagate(e);
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
            throw Throwables.propagate(e);
        }
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
            throw Throwables.propagate(e);
        }
        return fileName;
    }

    public static TR createInputRow(String label, String name, String value, boolean enabled, boolean required) {
        return createInputRow(label, name, value, enabled, required, Input.TEXT);
    }

    public static TR createCheckboxRow(String label, String name, boolean checked, boolean enabled, boolean required) {
        TR tr = new TR();
        tr.addElement(new TD(label).setClass(Resources.CLASS_LIST_TABLE_TD));
        Input input = new Input(Input.CHECKBOX, name);
        input.setChecked(checked);
        input.setDisabled(!enabled);
        if (required) {
            input.setClass(Resources.CLASS_REQUIRED);
        }
        tr.addElement(new TD(input).setClass(Resources.CLASS_LIST_TABLE_TD));
        return tr;
    }

    public static TR createSelectRow(String label, String name, Option[] options, boolean enabled, boolean required) {
        TR tr = new TR();
        tr.addElement(new TD(label).setClass(Resources.CLASS_LIST_TABLE_TD));
        Select select = new Select(name, options);
        select.setID(name);
        select.setDisabled(!enabled);
        return createSelectRow(label, select, required);
    }

    public static TR createSelectRow(String label, Select select, boolean required) {
        TR tr = new TR();
        tr.addElement(new TD(label).setClass(Resources.CLASS_LIST_TABLE_TD));
        TD td;
        if (required) {
            Div div = new Div();
            div.addElement(select);
            div.setClass(Resources.CLASS_REQUIRED);
            td = new TD(div);
        } else {
            td = new TD(select);
        }
        tr.addElement(td.setClass(Resources.CLASS_LIST_TABLE_TD));
        return tr;
    }

    public static TR createInputRow(String label, String name, String value, boolean enabled, boolean required, String type) {
        TR tr = new TR();
        TD labelTd = new TD(label);
        labelTd.setClass(Resources.CLASS_LIST_TABLE_TD);
        tr.addElement(labelTd);
        Input input = new Input(type, name, String.valueOf(value));
        input.setDisabled(!enabled);
        if (required) {
            input.setClass(Resources.CLASS_REQUIRED);
        }
        tr.addElement(new TD(input).setClass(Resources.CLASS_LIST_TABLE_TD));
        return tr;
    }

}
