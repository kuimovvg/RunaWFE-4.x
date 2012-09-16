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
package ru.runa.af.web.orgfunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.af.AuthenticationException;
import ru.runa.af.organizationfunction.ParamRenderer;
import ru.runa.delegate.DelegateFactory;

public class SwimlaneSubstitutionCriteriaRenderer implements ParamRenderer {

    private static final Log log = LogFactory.getLog(SwimlaneSubstitutionCriteriaRenderer.class);

    public boolean hasJSEditor() {
        return true;
    }

    public List<String[]> loadJSEditorData(Subject subject) {
        List<String[]> result = new ArrayList<String[]>();
        try {
            Set<String> swimlanes = DelegateFactory.getInstance().getDefinitionService()
                    .getAllSwimlanesNamesForAllProcessDefinition(subject);
            for (String swimlaneName : swimlanes) {
                result.add(new String[] { swimlaneName, swimlaneName });
            }
        } catch (AuthenticationException e) {
            log.error("", e);
        }
        return result;
    }

    public String getDisplayLabel(Subject subject, String value) {
        return value;
    }

    public boolean isValueValid(Subject subject, String value) {
        return value.trim().length() > 0;
    }

}
