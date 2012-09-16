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

import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.Profile;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.af.web.action.StrutsTestCase;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.action.ChangeActiveBatchPresentationAction;
import ru.runa.common.web.form.BatchPresentationForm;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.presentation.WFProfileStrategy;

/**
 *
 * Created 20.08.2005
 */
public class ChangeActiveBatchPresentationActionTest extends StrutsTestCase {

    private static final String RETURN_ACTION = "returnAction";

    private static final String BATCH_PRESENTATION_TYPE = WFProfileStrategy.PROCESS_TASK_BATCH_PRESENTATION_ID;

    private static final String NEW_BATCH_PRESENTATION_NAME = "New peresr";

    private BatchPresentation batchPresentation;

    private BatchPresentation newPresentation;

    private BatchPresentationForm form;

    private ChangeActiveBatchPresentationAction action;

    private Profile profile;

    public String getTestPrefix() {
        return getClass().getName();
    }

    protected void setUp() throws Exception {
        super.setUp();

        profile = ProfileHttpSessionHelper.getProfile(session);
        batchPresentation = profile.getActiveBatchPresentation(BATCH_PRESENTATION_TYPE);

        action = new ChangeActiveBatchPresentationAction();
        form = new BatchPresentationForm();
        form.setReturnAction(RETURN_ACTION);

        newPresentation = batchPresentation.clone();
        newPresentation.setBatchPresentationName(NEW_BATCH_PRESENTATION_NAME);
        profile.addBatchPresentation(newPresentation);
        DelegateFactory.getInstance().getProfileService().saveProfile(SubjectHttpSessionHelper.getActorSubject(session), profile);

        profile.setActiveBatchPresentation(BATCH_PRESENTATION_TYPE, batchPresentation.getBatchPresentationName());
    }

    protected void tearDown() throws Exception {
        profile.deleteBatchPresentation(newPresentation);
        super.tearDown();
    }

    public void testChangeActiveBatchPresentation() throws Exception {
        form.setBatchPresentationName(NEW_BATCH_PRESENTATION_NAME);
        form.setBatchPresentationId(BATCH_PRESENTATION_TYPE);

        ActionForward forward = action.execute(null, form, request, response);
        assertNotNull("testChangeActiveBatchPresentation returns null forward", forward);
        assertEquals("testChangeActiveBatchPresentation returns wrong forward", RETURN_ACTION, forward.getPath());
        assertNull("testChangeActiveBatchPresentation returns with errors", getGlobalErrors());

        assertEquals("testChangeActiveBatchPresentation doesn't set batchPresentation as active", NEW_BATCH_PRESENTATION_NAME, profile
                .getActiveBatchPresentation(BATCH_PRESENTATION_TYPE).getBatchPresentationName());

        profile.deleteBatchPresentation(newPresentation);

        assertEquals("testChangeActiveBatchPresentation doesn't set batchPresentation as active", batchPresentation.getBatchPresentationName(),
                profile.getActiveBatchPresentation(BATCH_PRESENTATION_TYPE).getBatchPresentationName());

        newPresentation = batchPresentation.clone();
        newPresentation.setBatchPresentationName(NEW_BATCH_PRESENTATION_NAME);
        profile.addBatchPresentation(newPresentation);
    }

    public void testChangeActiveBatchPresentationUnexistent() throws Exception {
        form.setBatchPresentationName(NEW_BATCH_PRESENTATION_NAME + "Unexistent");
        form.setBatchPresentationId(BATCH_PRESENTATION_TYPE);

        ActionForward forward = action.execute(null, form, request, response);
        assertNotNull("testChangeActiveBatchPresentation returns null forward", forward);
        assertEquals("testChangeActiveBatchPresentation returns wrong forward", RETURN_ACTION, forward.getPath());
        assertNotNull("testChangeActiveBatchPresentation returns with errors", getGlobalErrors());
        assertEquals("testChangeActiveBatchPresentation returns wrong errors size", 1, getGlobalErrors().size());

        assertEquals("testChangeActiveBatchPresentation tryed to change batchPresentation", batchPresentation.getBatchPresentationName(), profile
                .getActiveBatchPresentation(BATCH_PRESENTATION_TYPE).getBatchPresentationName());
    }
}
