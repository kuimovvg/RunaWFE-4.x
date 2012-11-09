/*
 * This file is part of the RUNA WFE project.
 * Copyright (C) 2004-2006, Joint stock company "RUNA Technology"
 * All rights reserved.
 * 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ru.runa.wf.logic.bot.textreport;

import java.io.InputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.xml.PathEntityResolver;
import ru.runa.wfe.commons.xml.XMLHelper;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.VariableFormat;

/**
 * Created on 2006
 * 
 */
public class TextReportSettingsXmlParser {

    private static final String FILE_NAME_ATTRIBUTE_NAME = "fileName";

    private static final String FILE_ENCODING_ATTRIBUTE_NAME = "fileEncoding";

    private static final String VARIABLE_NAME_ATTRIBUTE_NAME = "variableName";

    private static final String CONTENT_TYPE_ATTRIBUTE_NAME = "contentType";

    private static final String TEMPLATE_ELEMENT_NAME = "template";

    private static final String REPORT_ELEMENT_NAME = "report";

    private static final String VARIABLE_ELEMENT_NAME = "variable";

    private static final String FORMAT_CLASS_ATTRIBUTE_NAME = "formatClass";

    private static final String FORMAT_PATTERN_ATTRIBUTE_NAME = "pattern";

    private static final String REPLACEMENTS_ELEMENT_NAME = "replacements";

    private static final String REPLACEMENT_ELEMENT_NAME = "replacement";

    private static final String SOURCE_ATTRIBUTE_NAME = "source";

    private static final String DESTINATION_ATTRIBUTE_NAME = "dest";

    private static final String XML_FORMAT_ATTRIBUTE_NAME = "xmlFormat";

    private static final String APPLY_TO_REGEXP_ATTRIBUTE_NAME = "applyToRegexp";
    private static final PathEntityResolver PATH_ENTITY_RESOLVER = new PathEntityResolver("textreport.xsd");

    private TextReportSettingsXmlParser() {
        // prevents direct object instantiation
    }

    public static TextReportSettings read(InputStream inputStream) {
        try {
            TextReportSettings textReportSettings = new TextReportSettings();

            Document document = XMLHelper.getDocument(inputStream, PATH_ENTITY_RESOLVER);

            Node templateNode = document.getElementsByTagName(TEMPLATE_ELEMENT_NAME).item(0);
            textReportSettings.setTemplateFileName(templateNode.getAttributes().getNamedItem(FILE_NAME_ATTRIBUTE_NAME).getNodeValue());
            textReportSettings.setTemplateEncoding(templateNode.getAttributes().getNamedItem(FILE_ENCODING_ATTRIBUTE_NAME).getNodeValue());

            Node reportNode = document.getElementsByTagName(REPORT_ELEMENT_NAME).item(0);
            textReportSettings.setReportFileName(reportNode.getAttributes().getNamedItem(FILE_NAME_ATTRIBUTE_NAME).getNodeValue());
            textReportSettings.setReportEncoding(reportNode.getAttributes().getNamedItem(FILE_ENCODING_ATTRIBUTE_NAME).getNodeValue());
            textReportSettings.setReportContentType(reportNode.getAttributes().getNamedItem(CONTENT_TYPE_ATTRIBUTE_NAME).getNodeValue());
            textReportSettings.setReportVariableName(reportNode.getAttributes().getNamedItem(VARIABLE_NAME_ATTRIBUTE_NAME).getNodeValue());

            NodeList replacementsNodeList = document.getElementsByTagName(REPLACEMENTS_ELEMENT_NAME);
            if (replacementsNodeList.getLength() > 0) {
                Node replacementsNode = replacementsNodeList.item(0);
                textReportSettings.setXmlFormatSupport("true".equals(replacementsNode.getAttributes().getNamedItem(XML_FORMAT_ATTRIBUTE_NAME)
                        .getNodeValue()));
                textReportSettings.setApplyToRegexp("true".equals(replacementsNode.getAttributes().getNamedItem(APPLY_TO_REGEXP_ATTRIBUTE_NAME)
                        .getNodeValue()));
            }

            NodeList nodeList = document.getElementsByTagName(REPLACEMENT_ELEMENT_NAME);
            String[] replacementSources = new String[nodeList.getLength()];
            String[] replacementDestinations = new String[nodeList.getLength()];
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node replacementNode = nodeList.item(i);
                replacementSources[i] = replacementNode.getAttributes().getNamedItem(SOURCE_ATTRIBUTE_NAME).getNodeValue();
                replacementDestinations[i] = replacementNode.getAttributes().getNamedItem(DESTINATION_ATTRIBUTE_NAME).getNodeValue();
            }
            textReportSettings.setReplacements(replacementSources, replacementDestinations);

            nodeList = document.getElementsByTagName(VARIABLE_ELEMENT_NAME);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node variableNode = nodeList.item(i);
                String variableName = variableNode.getAttributes().getNamedItem(VARIABLE_NAME_ATTRIBUTE_NAME).getNodeValue();
                String formatClassName = variableNode.getAttributes().getNamedItem(FORMAT_CLASS_ATTRIBUTE_NAME).getNodeValue();
                String formatPattern = null;
                if (variableNode.getAttributes().getNamedItem(FORMAT_PATTERN_ATTRIBUTE_NAME) != null) {
                    formatPattern = variableNode.getAttributes().getNamedItem(FORMAT_PATTERN_ATTRIBUTE_NAME).getNodeValue();
                }

                if ((formatClassName == null) || (formatClassName.length() == 0)) {
                    formatClassName = StringFormat.class.getName();
                }
                VariableFormat format;
                if ((formatPattern == null) || (formatPattern.length() == 0)) {
                    format = FormatCommons.create(formatClassName, formatPattern);
                } else {
                    format = FormatCommons.create(formatClassName);
                }
                textReportSettings.addVariableFormat(variableName, format);
            }

            return textReportSettings;
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }

    }
}
