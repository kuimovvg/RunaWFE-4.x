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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationConsts;
import ru.runa.af.presentation.FieldDescriptor;
import ru.runa.af.presentation.Profile;
import ru.runa.af.web.action.StrutsTestCase;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.action.TableViewSetupFormAction;
import ru.runa.common.web.form.TableViewSetupForm;
import ru.runa.commons.ApplicationContextFactory;
import ru.runa.junit.WebArrayAssert;
import ru.runa.wf.presentation.WFProfileStrategy;

/**
 * Created on 19.10.2004
 * 
 */
public class TableViewSetupFormActionTest extends StrutsTestCase {

    @Override
    public String getTestPrefix() {
        return TableViewSetupFormActionTest.class.getName();
    }

    private static final String FORWARD_NAME = "manage_executors";

    private static final String TAG_NAME = WFProfileStrategy.PROCESS_INSTANCE_BATCH_PRESENTATION_ID;

    private TableViewSetupFormAction action;

    private ActionMapping mapping;

    private TableViewSetupForm form;

    public static Test suite() {
        return new TestSuite(TableViewSetupFormActionTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        action = new TableViewSetupFormAction();

        Map<String, String> forwards = new HashMap<String, String>();
        forwards.put(FORWARD_NAME, FORWARD_NAME);
        mapping = getActionMapping(forwards);
        mapping.setParameter("dispatch");
        form = new TableViewSetupForm();
        form.reset(mapping, request);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        ProfileHttpSessionHelper.removeProfile(session);
    }

    public void testApplyBatchPresentationNoSorting() throws Exception {
        Long[] ids = { 0L, 1L, 2L };
        form.setIds(ids);
        form.setFilteringIds(ids);
        form.setSortingIds(ids);
        form.setEditableFieldsValues(new String[] { "", "", "" });

        int[] displayPositionIds = { 0, 1, 0 };
        form.setDisplayPositionsIds(displayPositionIds);

        String[] sortingModes = { TableViewSetupForm.ASC_SORTING_MODE, TableViewSetupForm.ASC_SORTING_MODE, TableViewSetupForm.ASC_SORTING_MODE };
        form.setSortingModeNames(sortingModes);

        int[] sortPositionIds = { -1, -1, -1 };
        form.setSortPositionsIds(sortPositionIds);

        form.setReturnAction(FORWARD_NAME);
        form.setBatchPresentationId(TAG_NAME);

        ActionForward forward = action.apply(mapping, form, request, response);
        assertEquals("action.execute returns wrong forward", FORWARD_NAME, forward.getPath());
        assertNull("action.execute returns with errors", getGlobalErrors());
        Profile profile = ProfileHttpSessionHelper.getProfile(session);
        BatchPresentation batchPresentation = profile.getActiveBatchPresentation(TAG_NAME);

        checkFieldIdx(batchPresentation.getDisplayFields(), new int[] { 0, 2, 1 });
        checkFieldIdx(batchPresentation.getHiddenFields(), new int[] { 3, 4, 5, 6 });

        assertEquals("sortNames differs", 0, batchPresentation.getSortedFields().length);
    }

    public void testApplyBatchPresentationSorting() throws Exception {
        Long[] ids = { 0L, 1L, 2L };
        form.setIds(ids);
        form.setFilteringIds(ids);
        form.setSortingIds(ids);
        form.setEditableFieldsValues(new String[] { "", "", "" });

        int[] displayPositionIds = { -1, 0, 1 };
        form.setDisplayPositionsIds(displayPositionIds);

        String[] sortingModes = { TableViewSetupForm.ASC_SORTING_MODE, TableViewSetupForm.DSC_SORTING_MODE, TableViewSetupForm.ASC_SORTING_MODE };
        form.setSortingModeNames(sortingModes);

        int[] sortPositionIds = { 0, 0, -1 };
        form.setSortPositionsIds(sortPositionIds);

        form.setReturnAction(FORWARD_NAME);
        form.setBatchPresentationId(TAG_NAME);

        ActionForward forward = action.apply(mapping, form, request, response);
        assertEquals("action.execute returns wrong forward", FORWARD_NAME, forward.getPath());
        assertNull("action.execute returns with errors", getGlobalErrors());
        Profile profile = ProfileHttpSessionHelper.getProfile(session);
        BatchPresentation batchPresentation = profile.getActiveBatchPresentation(TAG_NAME);

        checkFieldIdx(batchPresentation.getDisplayFields(), new int[] { 1, 2 });
        checkFieldIdx(batchPresentation.getHiddenFields(), new int[] { 0, 3, 4, 5, 6 });

        FieldDescriptor[] fieldNames = batchPresentation.getAllFields();
        FieldDescriptor[] expectedSortedFields = { fieldNames[0], fieldNames[1] };
        WebArrayAssert.assertEqualArrays("sortFieldNames differs", expectedSortedFields, batchPresentation.getSortedFields());

        boolean[] expectedSortingModes = { BatchPresentationConsts.ASC, BatchPresentationConsts.DSC };
        WebArrayAssert.assertEqualArrays("sortModes differs", expectedSortingModes, batchPresentation.getFieldsToSortModes());
    }

    public void testApplyBatchPresentationUndisplayAll() throws Exception {
        Long[] ids = { 0L, 1L, 2L };
        form.setIds(ids);
        form.setFilteringIds(ids);
        form.setSortingIds(ids);
        form.setEditableFieldsValues(new String[] { "", "", "" });

        int[] displayPositionIds = { -1, -1, -1, -1 };
        form.setDisplayPositionsIds(displayPositionIds);

        String[] sortingModes = { TableViewSetupForm.ASC_SORTING_MODE, TableViewSetupForm.DSC_SORTING_MODE, TableViewSetupForm.ASC_SORTING_MODE };
        form.setSortingModeNames(sortingModes);

        int[] sortPositionIds = { 0, 0, 0 };
        form.setSortPositionsIds(sortPositionIds);

        form.setReturnAction(FORWARD_NAME);
        form.setBatchPresentationId(TAG_NAME);

        ActionForward forward = action.apply(mapping, form, request, response);
        assertEquals("action.execute returns wrong forward", FORWARD_NAME, forward.getPath());
        assertNull("action.execute returns with errors", getGlobalErrors());
        Profile profile = ProfileHttpSessionHelper.getProfile(session);
        BatchPresentation batchPresentation = profile.getActiveBatchPresentation(TAG_NAME);

        checkFieldIdx(batchPresentation.getDisplayFields(), new int[] {});
        checkFieldIdx(batchPresentation.getHiddenFields(), new int[] { 0, 1, 2, 3, 4, 5, 6 });

        FieldDescriptor[] fieldNames = batchPresentation.getAllFields();
        FieldDescriptor[] expectedSortedFields = { fieldNames[0], fieldNames[1], fieldNames[2] };
        WebArrayAssert.assertEqualArrays("sortFieldNames differs", expectedSortedFields, batchPresentation.getSortedFields());

        boolean[] expectedSortingModes = { BatchPresentationConsts.ASC, BatchPresentationConsts.DSC, BatchPresentationConsts.ASC };
        WebArrayAssert.assertEqualArrays("sortModes differs", expectedSortingModes, batchPresentation.getFieldsToSortModes());
    }

    public void testApplyBatchPresentationViewSize() throws Exception {
        Long[] ids = { 0L, 1L, 2L };
        form.setIds(ids);
        form.setFilteringIds(ids);
        form.setSortingIds(ids);
        form.setEditableFieldsValues(new String[] { "", "", "" });
        int[] displayPositionIds = { -1, -1, -1 };
        form.setDisplayPositionsIds(displayPositionIds);
        String[] sortingModes = { TableViewSetupForm.ASC_SORTING_MODE, TableViewSetupForm.DSC_SORTING_MODE, TableViewSetupForm.ASC_SORTING_MODE };
        form.setSortingModeNames(sortingModes);
        int[] sortPositionIds = { 0, 0, 0 };
        form.setSortPositionsIds(sortPositionIds);

        form.setReturnAction(FORWARD_NAME);
        form.setBatchPresentationId(TAG_NAME);
        form.setViewSize(50);

        ActionForward forward = action.apply(mapping, form, request, response);
        assertEquals("action.execute returns wrong forward", FORWARD_NAME, forward.getPath());
        assertNull("action.execute returns with errors", getGlobalErrors());
        Profile profile = ProfileHttpSessionHelper.getProfile(session);
        BatchPresentation batchPresentation = profile.getActiveBatchPresentation(TAG_NAME);

        assertEquals("returns wrong view size", 50, batchPresentation.getRangeSize());
    }

    public void testCreateNewBatchPresentation() throws Exception {
        Long[] ids = { 0L, 1L, 2L };
        form.setIds(ids);
        form.setFilteringIds(ids);
        form.setSortingIds(ids);
        form.setEditableFieldsValues(new String[] { "", "", "" });
        int[] displayPositionIds = { -1, -1, -1 };
        form.setDisplayPositionsIds(displayPositionIds);
        String[] sortingModes = { TableViewSetupForm.ASC_SORTING_MODE, TableViewSetupForm.DSC_SORTING_MODE, TableViewSetupForm.ASC_SORTING_MODE };
        form.setSortingModeNames(sortingModes);
        int[] sortPositionIds = { 0, 0, 0 };
        form.setSortPositionsIds(sortPositionIds);

        String newName = "newBP";

        form.setSaveAsBatchPresentationName(newName);
        form.setReturnAction(FORWARD_NAME);
        form.setBatchPresentationId(TAG_NAME);
        form.setViewSize(50);

        ActionForward forward = action.saveAs(mapping, form, request, response);
        assertEquals("action.execute returns wrong forward", FORWARD_NAME, forward.getPath());
        assertNull("action.execute returns with errors", getGlobalErrors());
        Profile profile = ProfileHttpSessionHelper.getProfile(session);
        BatchPresentation batchPresentation = profile.getActiveBatchPresentation(TAG_NAME);

        assertEquals("returns wrong name", newName, batchPresentation.getBatchPresentationName());
        assertEquals("returns wrong view size", 50, batchPresentation.getRangeSize());
    }

    public void testCreateNewBatchPresentationEmptyName() throws Exception {
        Long[] ids = { 0L, 1L, 2L };
        form.setIds(ids);
        form.setFilteringIds(ids);
        form.setSortingIds(ids);
        form.setEditableFieldsValues(new String[] { "", "", "" });
        int[] displayPositionIds = { -1, -1, -1 };
        form.setDisplayPositionsIds(displayPositionIds);
        String[] sortingModes = { TableViewSetupForm.ASC_SORTING_MODE, TableViewSetupForm.DSC_SORTING_MODE, TableViewSetupForm.ASC_SORTING_MODE };
        form.setSortingModeNames(sortingModes);
        int[] sortPositionIds = { 0, 0, 0 };
        form.setSortPositionsIds(sortPositionIds);

        String newName = "";
        if (ApplicationContextFactory.getDialectClassName().contains("Oracle")) {
            newName = " ";
        }

        form.setSaveAsBatchPresentationName(newName);
        form.setReturnAction(FORWARD_NAME);
        form.setBatchPresentationId(TAG_NAME);
        form.setViewSize(50);

        ActionForward forward = action.saveAs(mapping, form, request, response);
        assertEquals("action.execute returns wrong forward", FORWARD_NAME, forward.getPath());
        assertNull("action.execute returns with errors", getGlobalErrors());

        Profile profile = ProfileHttpSessionHelper.getProfile(session);
        BatchPresentation batchPresentation = profile.getActiveBatchPresentation(TAG_NAME);

        assertEquals("returns wrong name", newName, batchPresentation.getBatchPresentationName());
    }

    public void testDeleteBatchPresentation() throws Exception {
        Profile profile = ProfileHttpSessionHelper.getProfile(session);

        BatchPresentation[] batchPresentations = profile.getBatchPresentations(TAG_NAME);
        assertEquals("returns wrong length", 1, batchPresentations.length);

        String newName = "toDeleteBP";
        BatchPresentation presentation = profile.getActiveBatchPresentation(TAG_NAME).clone();
        presentation.setBatchPresentationName(newName);
        profile.addBatchPresentation(presentation);
        profile.setActiveBatchPresentation(TAG_NAME, newName);

        batchPresentations = profile.getBatchPresentations(TAG_NAME);
        assertEquals("returns wrong length", 2, batchPresentations.length);

        form.setReturnAction(FORWARD_NAME);
        form.setBatchPresentationId(TAG_NAME);

        ActionForward forward = action.delete(mapping, form, request, response);
        assertEquals("action.execute returns wrong forward", FORWARD_NAME, forward.getPath());
        assertNull("action.execute returns with errors", getGlobalErrors());

        batchPresentations = profile.getBatchPresentations(TAG_NAME);
        assertEquals("returns wrong length", 1, batchPresentations.length);
    }

    public void testDeleteBatchPresentationDefault() throws Exception {
        Profile profile = ProfileHttpSessionHelper.getProfile(session);

        BatchPresentation batchPresentation = profile.getActiveBatchPresentation(TAG_NAME);
        assertTrue("returns wrong ", batchPresentation.isDefault());

        form.setReturnAction(FORWARD_NAME);
        form.setBatchPresentationId(TAG_NAME);

        ActionForward forward = action.delete(mapping, form, request, response);
        assertEquals("action.execute returns wrong forward", FORWARD_NAME, forward.getPath());
        assertNull("action.execute returns with errors", getGlobalErrors());

        batchPresentation = profile.getActiveBatchPresentation(TAG_NAME);
        assertTrue("returns wrong ", batchPresentation.isDefault());
    }

    private void checkFieldIdx(FieldDescriptor[] fields, int[] fieldIdx) {
        assertEquals("Arrays length is different", fields.length, fieldIdx.length);
        for (int i = 0; i < fields.length; ++i) {
            assertEquals("Field has unexpected index", fields[i].fieldIdx, fieldIdx[i]);
        }
    }
}
