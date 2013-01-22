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

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;

/**
 * 
 * Semantic is defined in msword-report-task.xsd.
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

    public static MSWordReportTaskSettings read(String configuration) {
        Document document = XmlUtils.parseWithoutValidation(configuration);
        Element root = document.getRootElement();
        String templatePath = root.attributeValue(TEMPLATE_FILE_PATH_ELEMENT_NAME);
        String fileName = root.attributeValue(OUTPUT_VARIABLE_FILE_ELEMENT_NAME);
        String variableName = root.attributeValue(OUTPUT_VARIABLE_ELEMENT_NAME);
        MSWordReportTaskSettings wordReportSettings = new MSWordReportTaskSettings(templatePath, fileName, variableName);
        List<Element> mappingElements = root.elements(MAPPING_ELEMENT_NAME);
        for (Element mappingElement : mappingElements) {
            String bookmark = mappingElement.attributeValue(BOOKMARK_ATTRIBUTE_NAME);
            String variable = mappingElement.attributeValue(VARIABLE_ATTRIBUTE_NAME);
            String formatClassName = mappingElement.attributeValue(FORMAT_CLASS_ATTRIBUTE_NAME);
            String format = mappingElement.attributeValue(FORMAT_ATTRIBUTE_NAME); // TODO
                                                                                  // PEX
                                                                                  // OPT
            BookmarkVariableMapping bookmarkVariableMapping = new BookmarkVariableMapping(bookmark, variable, formatClassName, format);
            wordReportSettings.addBookmarkMapping(bookmarkVariableMapping);
        }
        return wordReportSettings;
    }

}
