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
package ru.runa.commons.web.action;

import java.util.HashMap;
import java.util.Map;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.web.action.StrutsTestCase;
import ru.runa.common.web.TabHttpSessionHelper;
import ru.runa.common.web.action.TabHeaderForwardAction;

/**
 * Created on 20.05.2005
 * 
 */
public class TabHeaderForwardActionTest extends StrutsTestCase {

    private static final String FORWARD_NAME = "TABLE_HEADER_FORWARD";

    private TabHeaderForwardAction action;

    private final String initialName = "old_name";

    private ActionMapping mapping;

    public String getTestPrefix() {
        return getClass().getName();
    }

    protected void setUp() throws Exception {
        super.setUp();

        action = new TabHeaderForwardAction();

        Map<String, String> forwards = new HashMap<String, String>();
        forwards.put(FORWARD_NAME, FORWARD_NAME);
        forwards.put("URI", FORWARD_NAME);
        forwards.put("parameter", FORWARD_NAME);
        mapping = getActionMapping(forwards);
        mapping.setParameter("");
    }

    public void testTabHeaderForwardNullName() throws Exception {
        TabHttpSessionHelper.setTabForwardName(initialName, request.getSession());
        assertEquals("testTabHeaderForward doesn't set tab forward name", initialName, TabHttpSessionHelper.getTabForwardName(request.getSession()));

        ActionForward forward = action.execute(mapping, null, request, response);
        assertNotNull("testTabHeaderForward returns null forward", forward);

        assertEquals("testTabHeaderForward set tab forward NULL name", initialName, TabHttpSessionHelper.getTabForwardName(request.getSession()));
    }
}
