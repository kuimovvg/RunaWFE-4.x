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
package ru.runa.wf.logic.bot.assigner;

import ru.runa.commons.ResourceCommons;

public class AssignerResources extends ResourceCommons {

    private static final String SWIMLANE_NAME_PROPERTY = "swimlaneName";

    private static final String ASSIGNER_FUNCTION_PROPERTY = "assignerFunction";

    public AssignerResources(String bundleName) {
        super(bundleName);
    }

    public AssignerResources(byte[] bundleName) {
        super(bundleName);
    }

    public String getAssignerFunction() {
        return readProperty(ASSIGNER_FUNCTION_PROPERTY);
    }

    public String getSwimlaneName() {
        return readProperty(SWIMLANE_NAME_PROPERTY);
    }
}
