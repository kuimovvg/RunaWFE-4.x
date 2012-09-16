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

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import ru.runa.InternalApplicationException;

/**
 * 
 * Created on 24.11.2006
 *
 */
public class MSWordReportTaskSettings {

    private final String templateFileLocation;

    private final String reportFileName;

    private final String reportVariableName;

    public MSWordReportTaskSettings(String templateFileLocation, String reportFileName, String reportVariableName) {
        this.templateFileLocation = templateFileLocation;
        this.reportFileName = reportFileName;
        this.reportVariableName = reportVariableName;
    }

    public String getReportFileName() {
        return reportFileName;
    }

    public String getReportVariableName() {
        return reportVariableName;
    }
    
    public String getTemplateFileLocation() {
		return templateFileLocation;
	}

    public String getTemplateFilePath() {
    	File file = new File(templateFileLocation);
    	if (file.exists()) {
    		return templateFileLocation;
    	}
    	try {
            file = new File(getClass().getResource(templateFileLocation).toURI());
            if (file.exists()) {
            	return file.getAbsolutePath();
            }
            throw new InternalApplicationException("No template found at location '" + templateFileLocation + "'");
        } catch (URISyntaxException e) {
            throw new InternalApplicationException("No template found at location '" + templateFileLocation + "'", e);
        }
    }
    
    public static void main(String[] args) {
    	File file = new File("C:/Program Files/RunaWFE/Simulation/server/default/conf/sample-template7.doc");
    	System.out.println(file.exists());
    	System.out.println(file.getAbsolutePath());
	}

    private final Map<String, BookmarkVariableMapping> bookmarkMapping = new HashMap<String, BookmarkVariableMapping>();

    public void addBookmarkMapping(BookmarkVariableMapping bookmarkVariableMapping) {
        bookmarkMapping.put(bookmarkVariableMapping.getBookmarkName(), bookmarkVariableMapping);
    }

    public String format(String bookmark, Map<String, Object> variableMap) {
        BookmarkVariableMapping bookmarkVariableMapping = bookmarkMapping.get(bookmark);
        if (bookmarkVariableMapping == null) {
            throw new IllegalArgumentException("bookmark " + bookmark + " is not defined in document " + templateFileLocation);
        }
        Object variableValue = variableMap.get(bookmarkVariableMapping.getVariableName());
        if (variableValue == null) {
            throw new IllegalArgumentException("variable " + bookmarkVariableMapping.getVariableName() + " is not defined");
        }
        return bookmarkVariableMapping.getFormat().format(variableValue);
    }

}
