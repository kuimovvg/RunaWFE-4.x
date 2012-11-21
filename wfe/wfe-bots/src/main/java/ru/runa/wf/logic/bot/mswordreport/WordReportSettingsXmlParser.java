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
package ru.runa.wf.logic.bot.mswordreport;

import java.io.InputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.xml.PathEntityResolver;
import ru.runa.wfe.commons.xml.XMLHelper;

/**
 * 
 * Created on 24.11.2006
 * 
 */
public class WordReportSettingsXmlParser {
    private static final String TEMPLATE_FILE_PATH_ELEMENT_NAME = "template-path";
    private static final String OUTPUT_VARIABLE_FILE_ELEMENT_NAME = "output-variable-file-name";
    private static final String OUTPUT_VARIABLE_ELEMENT_NAME = "output-variable";
    private static final String MAPPING_ELEMENT_NAME = "mapping";
    private static final String BOOKMARK_ATTRIBUTE_NAME = "bookmark";
    private static final String VARIABLE_ATTRIBUTE_NAME = "variable";
    private static final String FORMAT_CLASS_ATTRIBUTE_NAME = "format-class";
    private static final String FORMAT_ATTRIBUTE_NAME = "format";
    private static final String REPORT_TASK_ELEMENT_NAME = "report";
    private static final PathEntityResolver PATH_ENTITY_RESOLVER = new PathEntityResolver("msword-report-task.xsd");

    private WordReportSettingsXmlParser() {
    }

    public static MSWordReportTaskSettings read(String configurationPath) {
        InputStream inputStream = ClassLoaderUtil.getResourceAsStream(configurationPath, WordReportSettingsXmlParser.class);
        Document document = XMLHelper.getDocument(inputStream, PATH_ENTITY_RESOLVER);
        return parse(document);
    }

    public static MSWordReportTaskSettings read(InputStream inputStream) {
        Document document = XMLHelper.getDocument(inputStream, PATH_ENTITY_RESOLVER);
        return parse(document);
    }

    private static MSWordReportTaskSettings parse(Document document) {
        Element report = (Element) document.getElementsByTagName(REPORT_TASK_ELEMENT_NAME).item(0);
        String templatePath = report.getAttribute(TEMPLATE_FILE_PATH_ELEMENT_NAME);
        String fileName = report.getAttribute(OUTPUT_VARIABLE_FILE_ELEMENT_NAME);
        String variableName = report.getAttribute(OUTPUT_VARIABLE_ELEMENT_NAME);
        MSWordReportTaskSettings wordReportSettings = new MSWordReportTaskSettings(templatePath, fileName, variableName);
        NodeList variablesNodeList = document.getElementsByTagName(MAPPING_ELEMENT_NAME);
        for (int i = 0; i < variablesNodeList.getLength(); i++) {
            Element variableElement = (Element) variablesNodeList.item(i);
            String bookmark = variableElement.getAttribute(BOOKMARK_ATTRIBUTE_NAME);
            String variable = variableElement.getAttribute(VARIABLE_ATTRIBUTE_NAME);
            String formatClassName = variableElement.getAttribute(FORMAT_CLASS_ATTRIBUTE_NAME);
            String format = variableElement.getAttribute(FORMAT_ATTRIBUTE_NAME);
            BookmarkVariableMapping bookmarkVariableMapping = new BookmarkVariableMapping(bookmark, variable, formatClassName, format);
            wordReportSettings.addBookmarkMapping(bookmarkVariableMapping);
        }
        return wordReportSettings;
    }

}
