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

import ru.runa.af.presentation.Profile;
import ru.runa.af.web.action.StrutsTestCase;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.action.HideableBlockAction;
import ru.runa.common.web.form.HideableBlockForm;

/**
 * Created on 20.05.2005
 *
 *
 */
public class HideableBlockActionTest extends StrutsTestCase {

    private static final String FORWARD_NAME = "hideable_action";

    private ActionMapping mapping;

    private HideableBlockForm form;

    private HideableBlockAction action;

    private final String blockId = "hideable_block_id";

    public String getTestPrefix() {
        return getClass().getName();
    }

    protected void setUp() throws Exception {
        super.setUp();

        action = new HideableBlockAction();
        Map<String, String> forwards = new HashMap<String, String>();
        forwards.put(FORWARD_NAME, FORWARD_NAME);
        mapping = getActionMapping(forwards);
        form = new HideableBlockForm();
        form.reset(mapping, request);
        form.setReturnAction(FORWARD_NAME);
    }

    public void testHideableBlock() throws Exception {
        form.setHideableBlockId(blockId);

        Profile profile = ProfileHttpSessionHelper.getProfile(request.getSession());
        profile.changeBlockVisibility(blockId);

        assertEquals("testHideableBlock doesn't set block visible", true, profile.isBlockVisible(blockId));

        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("testHideableBlock returns null forward", forward);
        assertEquals("testHideableBlock returns wrong forward", FORWARD_NAME, forward.getPath());
        assertNull("testHideableBlock returns with errors", getGlobalErrors());

        assertEquals("testHideableBlock doesn't set block invisible", false, profile.isBlockVisible(blockId));

        forward = action.execute(mapping, form, request, response);
        assertNotNull("testHideableBlock returns null forward", forward);
        assertEquals("testHideableBlock returns wrong forward", FORWARD_NAME, forward.getPath());
        assertNull("testHideableBlock returns with errors", getGlobalErrors());

        assertEquals("testHideableBlock doesn't set block visible", true, profile.isBlockVisible(blockId));
    }

    public void testHideableBlockInvalidBlockName() throws Exception {
        form.setHideableBlockId("INVALID_BLOCK_ID");

        Profile profile = ProfileHttpSessionHelper.getProfile(request.getSession());
        profile.changeBlockVisibility(blockId);

        assertEquals("testHideableBlock doesn't set block visible", true, profile.isBlockVisible(blockId));

        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("testHideableBlock returns null forward", forward);
        assertEquals("testHideableBlock returns wrong forward", FORWARD_NAME, forward.getPath());
        //assertNotNull("testHideableBlock returns with errors", getGlobalErrors());

        assertEquals("testHideableBlock set block invisible", true, profile.isBlockVisible(blockId));
    }
}
