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
import org.apache.struts.action.ActionMessages;

import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.FieldDescriptor;
import ru.runa.af.presentation.Profile;
import ru.runa.af.web.action.StrutsTestCase;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.action.SetSortingAction;
import ru.runa.common.web.form.SetSortingForm;
import ru.runa.junit.WebArrayAssert;

/**
 * Created on 20.05.2005
 * 
 */
public class SetSortingActionTest extends StrutsTestCase {

    private static final String FORWARD_NAME = "sorting";

    private final String batchPresentationId = "listProcessDefinitionsForm";

    private ActionMapping mapping;

    private SetSortingForm form;

    private SetSortingAction action;

    public String getTestPrefix() {
        return getClass().getName();
    }

    protected void setUp() throws Exception {
        super.setUp();

        action = new SetSortingAction();
        Map<String, String> forwards = new HashMap<String, String>();
        forwards.put(FORWARD_NAME, FORWARD_NAME);
        mapping = getActionMapping(forwards);
        form = new SetSortingForm();
        form.reset(mapping, request);
        form.setReturnAction(FORWARD_NAME);
    }

    public void testSetSortingNewFieldId() throws Exception {
        int newFieldId = 1;
        BatchPresentation batchPresentation = testHelper.getProcessDefinitionBatchPresentation(batchPresentationId);
        int[] fieldsToSortIds = { 0 };
        boolean[] sortModes = { false };
        batchPresentation.setFieldsToSort(fieldsToSortIds, sortModes);

        checkFieldIdx(batchPresentation.getSortedFields(), fieldsToSortIds);
        WebArrayAssert.assertEqualArrays("testSetSorting doesn't set field modes to sort in batch presentation", sortModes, batchPresentation
                .getFieldsToSortModes());

        Profile profile = ProfileHttpSessionHelper.getProfile(request.getSession());
        profile.addBatchPresentation(batchPresentation);

        form.setId(new Long(newFieldId)); // field id
        form.setBatchPresentationId(batchPresentation.getBatchPresentationId());

        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("testSetSorting returns null forward", forward);
        assertEquals("testSetSorting returns wrong forward", FORWARD_NAME, forward.getPath());
        assertNull("testSetSorting returns with errors", getGlobalErrors());

        assertEquals("testSetSorting doesn't set field to first place", newFieldId, batchPresentation.getSortedFields()[0].fieldIdx);
        assertEquals("testSetSorting doesn't set fields to sort in batch presentation", fieldsToSortIds.length + 1, batchPresentation
                .getSortedFields().length);
        assertEquals("testSetSorting doesn't set field modes to sort in batch presentation", sortModes.length + 1, batchPresentation
                .getFieldsToSortModes().length);
    }

    public void testSetSortingNoneBatchPresentation() throws Exception {
        form.setId(0L);
        form.setBatchPresentationId("Fake");

        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("testSetSorting returns null forward", forward);
        assertEquals("testSetSorting returns wrong forward", FORWARD_NAME, forward.getPath());
        ActionMessages messages = getGlobalErrors();
        assertNotNull("testSetSorting returns with errors", getGlobalErrors());
        assertEquals("testSetSorting returns with wrong error count", 1, messages.size());
    }

    public void testSetSortingExistentFieldId() throws Exception {
        int newFieldId = 1;
        boolean fieldSortMode = true;
        BatchPresentation batchPresentation = testHelper.getProcessDefinitionBatchPresentation(batchPresentationId);
        int[] fieldsToSortIds = { 0, newFieldId };
        boolean[] sortModes = { false, fieldSortMode };
        batchPresentation.setFieldsToSort(fieldsToSortIds, sortModes);

        checkFieldIdx(batchPresentation.getSortedFields(), fieldsToSortIds);
        WebArrayAssert.assertEqualArrays("testSetSorting doesn't set field modes to sort in batch presentation", sortModes, batchPresentation
                .getFieldsToSortModes());

        Profile profile = ProfileHttpSessionHelper.getProfile(request.getSession());
        profile.addBatchPresentation(batchPresentation);

        form.setId(new Long(newFieldId)); // field id
        form.setBatchPresentationId(batchPresentation.getBatchPresentationId());

        ActionForward forward = action.execute(mapping, form, request, response);
        assertNotNull("testSetSorting returns null forward", forward);
        assertEquals("testSetSorting returns wrong forward", FORWARD_NAME, forward.getPath());
        assertNull("testSetSorting returns with errors", getGlobalErrors());

        assertEquals("testSetSorting doesn't set field to first place", newFieldId, batchPresentation.getSortedFields()[0].fieldIdx);
        assertEquals("testSetSorting doesn't set field mode to first place", !fieldSortMode, batchPresentation.getFieldsToSortModes()[0]);
        assertEquals("testSetSorting doesn't set fields to sort in batch presentation", fieldsToSortIds.length,
                batchPresentation.getSortedFields().length);
        assertEquals("testSetSorting doesn't set field modes to sort in batch presentation", sortModes.length, batchPresentation
                .getFieldsToSortModes().length);
    }

    private void checkFieldIdx(FieldDescriptor[] fields, int[] fieldIdx) {
        assertEquals("Arrays length is different", fields.length, fieldIdx.length);
        for (int i = 0; i < fields.length; ++i) {
            assertEquals("Field has unexpected index", fields[i].fieldIdx, fieldIdx[i]);
        }
    }
}
