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

package ru.runa.wf.web.action;

import org.apache.struts.action.ActionForward;

import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.web.action.StrutsTestCase;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.wf.presentation.WFProfileStrategy;
import ru.runa.wf.web.form.PagingForm;

/**
 */
public class SetViewPageActionTest extends StrutsTestCase {

    private static final String RETURN_ACTION = "returnAction";

    private static final String BATCH_PRESENTATION_TYPE = WFProfileStrategy.PROCESS_TASK_BATCH_PRESENTATION_ID;

    public String getTestPrefix() {
        return SetViewPageActionTest.class.getName();
    }

    private SetViewPageAction action;

    private PagingForm form;

    private BatchPresentation batchPresentation;

    protected void setUp() throws Exception {
        super.setUp();
        action = new SetViewPageAction();

        form = new PagingForm();
        form.setReturnAction(RETURN_ACTION);
        form.setBatchPresentationId(BATCH_PRESENTATION_TYPE);

        batchPresentation = ProfileHttpSessionHelper.getProfile(request.getSession()).getActiveBatchPresentation(BATCH_PRESENTATION_TYPE);
    }

    public void testSetViewPage() throws Exception {
        batchPresentation.setPageNumber(3);
        int pageNumber = 1;
        form.setViewPage(pageNumber);

        ActionForward forward = action.execute(null, form, request, response);
        assertNotNull("testSetViewPage returns null forward", forward);
        assertEquals("testSetViewPage returns wrong forward", RETURN_ACTION, forward.getPath());
        assertNull("testSetViewPage returns with errors", getGlobalErrors());

        assertEquals("testSetViewPage returns wrong page number", pageNumber, batchPresentation.getPageNumber());
    }

    public void testSetViewPageNullPage() throws Exception {
        batchPresentation.setPageNumber(3);

        ActionForward forward = action.execute(null, form, request, response);
        assertNotNull("testSetViewPage returns null forward", forward);
        assertEquals("testSetViewPage returns wrong forward", RETURN_ACTION, forward.getPath());
        assertNull("testSetViewPage returns with errors", getGlobalErrors());
    }

    public void testSetViewPageInvalidPage() throws Exception {
        batchPresentation.setPageNumber(3);
        form.setViewPage(-100);

        ActionForward forward = action.execute(null, form, request, response);
        assertNotNull("testSetViewPage returns null forward", forward);
        assertEquals("testSetViewPage returns wrong forward", RETURN_ACTION, forward.getPath());
        // should we throw exeption or set to 1st page in this case?
        assertNull("testSetViewPage returns with errors", getGlobalErrors());
    }
}
