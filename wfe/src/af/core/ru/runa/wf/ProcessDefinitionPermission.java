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
 * Represents Permissions on a {@link ru.runa.wf.ProcessDefinition}
 * Created on 21.09.2004
 */
public class ProcessDefinitionPermission extends Permission {

    private static final long serialVersionUID = 4981030703856496613L;
    public static final String REDEPLOY_DEFINITION_PERMISSION_NAME = "permission.redeploy_definition";
    public static final String UNDERPLOY_DEFINITION_PERMISSOIN_NAME = "permission.undeploy_definition";
    public static final String START_PROCESS_PERMISSION_NAME = "permission.start_process";
    public static final String READ_STARTED_PROCESS_INSTANCE_PERMISSION_NAME = "permission.read_instance";
    public static final String CANCEL_STARTED_PROCESS_INSTANCE_PERMISSION_NAME = "permission.cancel_instance";

    public static final Permission REDEPLOY_DEFINITION = new ProcessDefinitionPermission((byte) 2, REDEPLOY_DEFINITION_PERMISSION_NAME);
    public static final Permission UNDEPLOY_DEFINITION = new ProcessDefinitionPermission((byte) 3, UNDERPLOY_DEFINITION_PERMISSOIN_NAME);
    public static final Permission START_PROCESS = new ProcessDefinitionPermission((byte) 4, START_PROCESS_PERMISSION_NAME);

    //UGLY HACK the following permission would be given to executors on created process instance
    public static final Permission READ_STARTED_INSTANCE = new ProcessDefinitionPermission((byte) 5, READ_STARTED_PROCESS_INSTANCE_PERMISSION_NAME);
    public static final Permission CANCEL_STARTED_INSTANCE = new ProcessDefinitionPermission((byte) 6,
            CANCEL_STARTED_PROCESS_INSTANCE_PERMISSION_NAME);

    private static final List<Permission> ALL_PERMISSIONS = fillPermissions();

    protected ProcessDefinitionPermission(byte maskPower, String name) {
        super(maskPower, name);
    }

    public ProcessDefinitionPermission() {
        super();
    }

    @Override
    public List<Permission> getAllPermissions() {
        return Lists.newArrayList(ALL_PERMISSIONS);
    }

    private static List<Permission> fillPermissions() {
        List<Permission> superPermissions = new Permission().getAllPermissions();
        List<Permission> result = Lists.newArrayList(superPermissions);
        result.add(REDEPLOY_DEFINITION);
        result.add(UNDEPLOY_DEFINITION);
        result.add(START_PROCESS);
        result.add(READ_STARTED_INSTANCE);
        result.add(CANCEL_STARTED_INSTANCE);
        return result;
    }

}
