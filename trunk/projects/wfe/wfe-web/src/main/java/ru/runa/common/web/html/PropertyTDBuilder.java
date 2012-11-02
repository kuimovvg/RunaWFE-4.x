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
package ru.runa.common.web.html;

import javax.servlet.jsp.JspException;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;

import ru.runa.common.web.Resources;
import ru.runa.common.web.html.TDBuilder.Env.IdentifiableExtractor;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.security.Permission;

public class PropertyTDBuilder extends BaseTDBuilder {

    static enum AUTH_STATE {
        ALWAYS_ENABLE, ALWAYS_DISABLE, ASK_WFE
    }

    private final String propertyName;
    private final AUTH_STATE authState;

    public PropertyTDBuilder(Permission permission, String propertyName) {
        super(permission);
        this.propertyName = propertyName;
        authState = permission.getName() == null ? AUTH_STATE.ALWAYS_ENABLE : AUTH_STATE.ASK_WFE;
    }

    public PropertyTDBuilder(Permission permission, String propertyName, Boolean isAlwaysDisabled) {
        super(permission);
        this.propertyName = propertyName;
        authState = isAlwaysDisabled ? AUTH_STATE.ALWAYS_DISABLE : AUTH_STATE.ASK_WFE;
    }

    public PropertyTDBuilder(Permission permission, String propertyName, IdentifiableExtractor identifiableExtractor) {
        super(permission, identifiableExtractor);
        this.propertyName = propertyName;
        authState = permission.getName() == null ? AUTH_STATE.ALWAYS_ENABLE : AUTH_STATE.ASK_WFE;
    }

    @Override
    public TD build(Object object, Env env) throws JspException {
        ConcreteElement element;
        if (authState == AUTH_STATE.ALWAYS_ENABLE || (authState == AUTH_STATE.ASK_WFE && isEnabled(object, env))) {
            element = new A(env.getURL(object), getValue(object, env));
            if (object instanceof WfDefinition && env.getConfirmationMessage(((WfDefinition) object).getId()) != null) {
                element.addAttribute("onclick", env.getConfirmationMessage(((WfDefinition) object).getId()));
            }
        } else {
            element = new StringElement(getValue(object, env));
        }
        TD td = new TD();
        td.setClass(Resources.CLASS_LIST_TABLE_TD);
        td.addElement(element);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        try {
            String property = readProperty(object, propertyName, false);
            return property == null ? "" : property;
        } catch (JspException e) {
            return "";
        }
    }

    @Override
    public String[] getSeparatedValues(Object object, Env env) {
        return new String[] { getValue(object, env) };
    }

    @Override
    public int getSeparatedValuesCount(Object object, Env env) {
        return 1;
    }
}
