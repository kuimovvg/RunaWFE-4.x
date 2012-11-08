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

import ru.runa.service.delegate.DelegateFactory;
import ru.runa.wfe.os.ParamRenderer;

public class SwimlaneSubstitutionCriteriaRenderer implements ParamRenderer {

    @Override
    public boolean hasJSEditor() {
        return true;
    }

    @Override
    public List<String[]> loadJSEditorData(Subject subject) {
        List<String[]> result = new ArrayList<String[]>();
        Set<String> swimlanes = DelegateFactory.getDefinitionService().getAllSwimlanesNamesFromAllDefinitions(subject);
        for (String swimlaneName : swimlanes) {
            result.add(new String[] { swimlaneName, swimlaneName });
        }
        return result;
    }

    @Override
    public String getDisplayLabel(Subject subject, String value) {
        return value;
    }

    @Override
    public boolean isValueValid(Subject subject, String value) {
        return value.trim().length() > 0;
    }

}
