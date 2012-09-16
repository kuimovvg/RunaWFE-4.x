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
package ru.runa.wf;

import java.util.List;

import ru.runa.af.Permission;

import com.google.common.collect.Lists;

/**
 * Represents Permissions on a {@link ru.runa.wf.ProcessInstanceStub}Created on 02.11.2004
 * 
 */
public class ProcessInstancePermission extends Permission {

    private static final long serialVersionUID = -6364900233533057786L;

    public static final String CANCEL_PROCESS_INSTANCE_PERMISSION_NAME = "permission.cancel_instance";

    public static final Permission CANCEL_INSTANCE = new ProcessInstancePermission((byte) 2, CANCEL_PROCESS_INSTANCE_PERMISSION_NAME);

    private static final List<Permission> ALL_PERMISSIONS = fillPermissions();

    public ProcessInstancePermission(byte maskPower, String name) {
        super(maskPower, name);
    }

    public ProcessInstancePermission() {
        super();
    }

    @Override
    public List<Permission> getAllPermissions() {
        return Lists.newArrayList(ALL_PERMISSIONS);
    }

    private static List<Permission> fillPermissions() {
        List<Permission> superPermissions = new Permission().getAllPermissions();
        List<Permission> result = Lists.newArrayList(superPermissions);
        result.add(CANCEL_INSTANCE);
        return result;
    }

}
