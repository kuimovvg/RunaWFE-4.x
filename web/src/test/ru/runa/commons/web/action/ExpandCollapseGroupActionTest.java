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

import org.apache.struts.action.ActionForward;

import ru.runa.af.web.action.StrutsTestCase;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.action.ExpandCollapseGroupAction;
import ru.runa.common.web.form.GroupForm;
import ru.runa.wf.presentation.WFProfileStrategy;

/**
 */
public class ExpandCollapseGroupActionTest extends StrutsTestCase {
    private static final String RETURN_ACTION = "returnAction";

    private static final String BATCH_PRESENTATION_TYPE = WFProfileStrategy.PROCESS_TASK_BATCH_PRESENTATION_ID;

    private GroupForm form;

    private ExpandCollapseGroupAction action;

    //private BatchPresentation batchPresentation;

    //private Profile profile;

    public String getTestPrefix() {
        return getClass().getName();
    }

    protected void setUp() throws Exception {
        super.setUp();

        //profile = ProfileHttpSessionHelper.getProfile(session);

        action = new ExpandCollapseGroupAction();
        form = new GroupForm();
        form.setReturnAction(RETURN_ACTION);

        //batchPresentation = profile.getActiveBatchPresentation(BATCH_PRESENTATION_TYPE);
    }

    protected void tearDown() throws Exception {
        //batchPresentation = null;
        super.tearDown();
    }

    public void testExpandCollapseGroup() throws Exception {
        String groupName = "groupXXX";

        form.setBatchPresentationId(BATCH_PRESENTATION_TYPE);
        form.setGroupId(groupName);
        form.setActionId(GroupForm.GROUP_ACTION_EXPAND);
        ProfileHttpSessionHelper.getProfile(session).getActiveBatchPresentation(BATCH_PRESENTATION_TYPE).setGroupBlockStatus(groupName, false);

        ActionForward forward = action.execute(null, form, request, response);
        assertNotNull("testExpandCollapseGroup returns null forward", forward);
        assertEquals("testExpandCollapseGroup returns wrong forward", RETURN_ACTION, forward.getPath());
        assertNull("testExpandCollapseGroup returns with errors", getGlobalErrors());
        assertTrue("testExpandCollapseGroup doesn't set group as visible", ProfileHttpSessionHelper.getProfile(session).getActiveBatchPresentation(
                BATCH_PRESENTATION_TYPE).isGroupBlockExpanded(groupName));

        form = new GroupForm();
        form.setReturnAction(RETURN_ACTION);
        form.setBatchPresentationId(BATCH_PRESENTATION_TYPE);
        form.setGroupId(groupName);
        form.setActionId(GroupForm.GROUP_ACTION_COLLAPSE);

        forward = action.execute(null, form, request, response);
        assertNotNull("testExpandCollapseGroup returns null forward", forward);
        assertEquals("testExpandCollapseGroup returns wrong forward", RETURN_ACTION, forward.getPath());
        assertNull("testExpandCollapseGroup returns with errors", getGlobalErrors());
        assertFalse("testExpandCollapseGroup doesn't set group as invisible", ProfileHttpSessionHelper.getProfile(session)
                .getActiveBatchPresentation(BATCH_PRESENTATION_TYPE).isGroupBlockExpanded(groupName));
    }

    public void testExpandCollapseGroupNullGroupName() throws Exception {
        // TODO this is not usecase since form validates groupName
        form.setBatchPresentationId(BATCH_PRESENTATION_TYPE);
        String groupName = null;
        form.setGroupId(groupName);
        form.setActionId(GroupForm.GROUP_ACTION_EXPAND);

        ActionForward forward = action.execute(null, form, request, response);
        assertNotNull("testExpandCollapseGroup returns null forward", forward);
        assertEquals("testExpandCollapseGroup returns wrong forward", RETURN_ACTION, forward.getPath());
        assertNull("testExpandCollapseGroup returns with errors", getGlobalErrors());
    }
}
