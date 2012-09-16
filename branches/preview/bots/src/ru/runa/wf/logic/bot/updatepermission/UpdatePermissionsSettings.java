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
package ru.runa.wf.logic.bot.updatepermission;

import java.util.Collection;

import ru.runa.af.Permission;

public class UpdatePermissionsSettings {

    private final Collection<Permission> permissions;

    private final String[] orgFunctions;

    private String method;

    private String conditionVarName;

    private String conditionVarValue;

    public static final String METHOD_ADD_NAME = "add";

    public static final String METHOD_SET_NAME = "set";

    public static final String METHOD_DELETE_NAME = "delete";

    public UpdatePermissionsSettings(String[] orgFunctions, String method, Collection<Permission> permissions) {
        this.orgFunctions = orgFunctions;
        setMethod(method);
        this.permissions = permissions;
    }

    private void setMethod(String method) {
        if (METHOD_ADD_NAME.equals(method) || METHOD_SET_NAME.equals(method) || METHOD_DELETE_NAME.equals(method)) {
            this.method = method;
        } else {
            throw new IllegalArgumentException("Unknown method name " + method);
        }
    }

    public void setCondition(String conditionVarName, String conditionVarValue) {
        this.conditionVarName = conditionVarName;
        this.conditionVarValue = conditionVarValue;
    }

    public boolean isConditionExists() {
        return ((conditionVarName != null) && (conditionVarValue != null));
    }

    public String getConditionVarName() {
        checkConditionExists();
        return conditionVarName;
    }

    public String getConditionVarValue() {
        checkConditionExists();
        return conditionVarValue;
    }

    private void checkConditionExists() {
        if (!isConditionExists()) {
            throw new IllegalStateException("Condition does not exist.");
        }
    }

    public String[] getOrgFunctions() {
        return orgFunctions;
    }

    public Collection<Permission> getPermissions() {
        return permissions;
    }

    public String getMethod() {
        return method;
    }
}
