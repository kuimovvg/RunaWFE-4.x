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
package ru.runa.af.organizationfunction;

import java.util.List;

import javax.security.auth.Subject;

/**
 * Used in substitutions (web-interface).
 * Since 3.2.1
 */
public interface ParamRenderer {

    /**
     * Returns user-friendly display label for parameter value
     */
    public String getDisplayLabel(Subject subject, String value);

    /**
     * Whether parameter has its own javascript editor
     */
    public boolean hasJSEditor();

    /**
     * Called from ajax serlvlet during JS editor initialization
     */
    public List<String[]> loadJSEditorData(Subject subject) throws Exception;

    /**
     * Validates parameter value
     */
    public boolean isValueValid(Subject subject, String value);
}
