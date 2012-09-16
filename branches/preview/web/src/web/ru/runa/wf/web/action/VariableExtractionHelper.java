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
package ru.runa.wf.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

import com.google.common.collect.Maps;

import ru.runa.InternalApplicationException;
import ru.runa.commons.format.FormatCommons;
import ru.runa.commons.format.WebFormat;
import ru.runa.commons.ftl.FtlTagVariableHandler;
import ru.runa.wf.FileVariable;
import ru.runa.wf.form.Interaction;
import ru.runa.wf.form.VariableDefinition;
import ru.runa.wf.web.VariablesFormatException;
import ru.runa.wf.web.forms.format.BooleanFormat;
import ru.runa.wf.web.forms.format.StringFormat;

/**
 * Created on 18.07.2005
 * 
 */
@SuppressWarnings("unchecked")
class VariableExtractionHelper {
    private static final String DEFAULT_FORMAT_CLASS_NAME = StringFormat.class.getName();

    private VariableExtractionHelper() {
    }

    public static Map<String, String[]> extractAllAvailableVariables(ActionForm actionForm) {
        Hashtable<String, Object> hashtable = actionForm.getMultipartRequestHandler().getAllElements();
        Map<String, String[]> variablesMap = new HashMap<String, String[]>();
        for (String varName : hashtable.keySet()) {
            Object value = hashtable.get(varName);
            if (value instanceof FormFile) {
                // we could not fulfill in future this type of the input on the
                // web page (access restriction), so discard it
                continue;
            } else {
                variablesMap.put(varName, (String[]) value);
            }
        }
        return variablesMap;
    }

    public static Map<String, Object> extractVariables(HttpSession session, ActionForm actionForm, Interaction interaction) throws VariablesFormatException {
        try {
            Hashtable<String, Object> hashtable = actionForm.getMultipartRequestHandler().getAllElements();
            List<String> formatErrorsForFields = new ArrayList<String>();

            Map<String, Object> variablesMap = Maps.newHashMap();
            for (VariableDefinition variableDefinition : interaction.getVariables().values()) {
                Object value = hashtable.get(variableDefinition.getName());
                // in case from contains not optional check box with boolean
                // format we must add boolean value as variable manually since
                // HTTP FORM doesn't pass unchecked variables.
                if (BooleanFormat.class.getName().equals(variableDefinition.getFormat())) {
                    if (value == null) {
                        value = new String[] { Boolean.FALSE.toString() };
                    }
                }
                if (value == null) {
                    // we put this validation in logic
                    continue;
                }
                Object variableValue = null;
                if (value instanceof FormFile) {
                    FormFile formFile = (FormFile) value;
                    if (formFile.getFileSize() > 0) {
                        String contentType = formFile.getContentType();
                        if (contentType == null) {
                            contentType = "application/octet-stream";
                        }
                        variableValue = new FileVariable(formFile.getFileName(), formFile.getFileData(), contentType);
                    }
                } else {
                    String[] valuesToFormat = (String[]) value;
                    try {
                        String formatClassName = variableDefinition.getFormat();
                        if (formatClassName.length() == 0) {
                            formatClassName = DEFAULT_FORMAT_CLASS_NAME;
                        }
                        WebFormat format = FormatCommons.create(formatClassName);
                        variableValue = format.parse(valuesToFormat);
                    } catch (Exception e) {
                        if (valuesToFormat[0].length() > 0) {
                            // in other case we put validation in logic
                            formatErrorsForFields.add(variableDefinition.getName());
                        }
                    }
                }
                if (variableValue != null) {
                    FtlTagVariableHandler handler = (FtlTagVariableHandler) session.getAttribute(FtlTagVariableHandler.HANDLER_KEY_PREFIX
                            + variableDefinition.getName());
                    if (handler != null) {
                        variableValue = handler.handle(variableValue);
                    }
                    variablesMap.put(variableDefinition.getName(), variableValue);
                }
            }

            if (formatErrorsForFields.size() > 0) {
                throw new VariablesFormatException(formatErrorsForFields);
            }
            return variablesMap;
        } catch (VariablesFormatException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }
}
