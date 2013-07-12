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

package ru.runa.wfe.var.format;

import java.util.Date;
import java.util.HashMap;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.FileVariable;
import ru.runa.wfe.var.ISelectable;
import ru.runa.wfe.var.VariableDefinition;

import com.google.common.collect.Maps;

public class FormatCommons {

    public static VariableFormat create(String className) {
        return ClassLoaderUtil.instantiate(className);
    }

    public static VariableFormat create(VariableDefinition variableDefinition) {
        if (variableDefinition == null) {
            return create(StringFormat.class.getName());
        }
        VariableFormat format = create(variableDefinition.getFormatClassName());
        if (format instanceof VariableFormatContainer) {
            ((VariableFormatContainer) format).setComponentClassNames(variableDefinition.getFormatComponentClassNames());
        }
        return format;
    }

    public static String getVarOut(User user, Object object, WebHelper webHelper, Long instanceId, String name, int listIndex, Object mapKey) {
        String value;
        if (object instanceof ISelectable) {
            value = ((ISelectable) object).getLabel();
        } else if (object instanceof Date) {
            value = CalendarUtil.formatDate((Date) object);
        } else if (object instanceof FileVariable) {
            value = FileFormat.getHtml(((FileVariable) object).getName(), webHelper, instanceId, name, listIndex, mapKey);
        } else if (object == null) {
            value = "";
        } else if (object instanceof Executor) {
            Executor executor = (Executor) object;
            if (ApplicationContextFactory.getPermissionDAO().isAllowed(user, Permission.READ, executor)) {
                HashMap<String, Object> params = Maps.newHashMap();
                params.put("id", executor.getId());
                String href = webHelper.getActionUrl("/manage_executor", params);
                return "<a href=\"" + href + "\">" + executor.getLabel() + "</>";
            } else {
                return executor.getLabel();
            }
        } else {
            value = String.valueOf(object);
        }
        return value;
    }

}
