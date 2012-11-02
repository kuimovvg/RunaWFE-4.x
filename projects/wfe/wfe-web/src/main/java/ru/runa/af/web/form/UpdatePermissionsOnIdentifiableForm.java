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
package ru.runa.af.web.form;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.common.web.Messages;
import ru.runa.common.web.form.IdsForm;

import com.google.common.collect.Sets;

/**
 * Created on 25.08.2004
 * 
 * @struts:form name = "updatePermissionsOnIdentifiableForm"
 */
public class UpdatePermissionsOnIdentifiableForm extends IdsForm {
    private static final long serialVersionUID = -8537078929694016589L;

    public static final String ON_VALUE = "on";

    public static final String EXECUTOR_INPUT_NAME_PREFIX = "executor";

    public static final String PERMISSION_INPUT_NAME_PREFIX = "permission";

    private Map<Long, Object> executorsMap;

    public Map<Long, Object> getExecutorsMap() {
        return executorsMap;
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        executorsMap = new HashMap<Long, Object>();
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = super.validate(mapping, request);
        if (getExecutorsMap() == null) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(Messages.ERROR_WEB_CLIENT_NULL_VALUE));
        }
        return errors;
    }

    /**
     * this method is used by Struts map backed forms
     * 
     * @param executorId
     * @param permissionObject
     */
    public void setExecutor(String executorId, Object permissionObject) {
        executorsMap.put(Long.valueOf(executorId), permissionObject);
    }

    /**
     * this method is used by Struts map backed forms
     * 
     * @param executorId
     * @return
     */
    public Object getExecutor(String executorId) {
        Object o = executorsMap.get(new Long(executorId));
        if (o == null) {
            o = new Permissions();
            setExecutor(executorId, o);
        }
        return o;
    }

    public Set<Long> getPermissionMasks(Long executorId) {
        Object o = executorsMap.get(executorId);
        if (o == null) {
            return Sets.newHashSet();
        }
        return ((Permissions) o).permissionsSet;
    }

    static public class Permissions {
        private final Set<Long> permissionsSet;

        public Permissions() {
            permissionsSet = new HashSet<Long>();
        }

        public Long[] getMasksOld() {
            Long[] masks = new Long[permissionsSet.size()];
            int i = 0;
            for (Long element : permissionsSet) {
                masks[i] = element;
            }
            return masks;
        }

        /**
         * this method is used by Struts map backed forms
         * 
         * @param permissionMask
         * @param value
         */
        public void setPermission(String permissionMask, Object value) {
            if (ON_VALUE.equals(value)) {
                permissionsSet.add(new Long(permissionMask));
            }
        }

        /**
         * this method is used by Struts map backed forms
         * 
         * @param permissionMask
         * @return
         */
        public Object getPermission(String permissionMask) {
            return permissionMask;
        }
    }
}
