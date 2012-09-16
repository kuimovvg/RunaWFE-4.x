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
package ru.runa.batchpresentation;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.Group;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.filter.FilterCriteria;
import ru.runa.af.service.ExecutorService;
import ru.runa.delegate.DelegateFactory;
import ru.runa.junit.WebArrayAssert;

/**
 * Created on 20.05.2005
 * 
 */
public class ExecutorDelegateGetGroupsInThatExecutorNotPresentTest extends ServletTestCase {
    private ExecutorService executorService;

    private BatchPresentation batchPresentation;

    private BatchPresentationTestHelper helper;

    public static Test suite() {
        return new TestSuite(ExecutorDelegateGetGroupsInThatExecutorNotPresentTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        executorService = DelegateFactory.getInstance().getExecutorService();

        helper = new BatchPresentationTestHelper(getClass().getName());
        helper.createTestGroups();
        helper.createViewerExecutor();
        helper.createSubjectedExecutor();
        helper.setREADPermissionsToViewerActorOnTestGroups();
        helper.setREADPermissionsToViewerActorOnSubjectedActor();

        batchPresentation = helper.getExecutorBatchPresentation();

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.releaseResources();
        executorService = null;
        super.tearDown();
    }

    public void testGetGroupsInThatExecutorNotPresent_CriteriaFilterName() throws Exception {
        int i = 2;
        int[] filteredFields = new int[] { 0 };
        Map<Integer, FilterCriteria> filteredTemplates = helper.adaptFilterFields(filteredFields,
                new String[] { helper.getTestGroups().get(i).getName() }, batchPresentation);
        batchPresentation.setFilteredFieldsMap(filteredTemplates);
        List<Group> groups = executorService.getExecutorGroups(helper.getViewerSubject(), helper.getSubjectedActor(), batchPresentation, true);
        assertEquals("array length differs from expected", 1, groups.size());
        assertEquals("executor differs from expected", helper.getTestGroups().get(i), groups.get(0));
    }

    public void testGetGroupsInThatExecutorNotPresent_CriteriaSortDescriptionAsc() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 2 }, new boolean[] { true });
        List<Group> groups = executorService.getExecutorGroups(helper.getViewerSubject(), helper.getSubjectedActor(), batchPresentation, true);
        WebArrayAssert.assertEqualArrays("executor names arrays are not equals", helper.sortExecutorsArray(helper.getTestGroups(),
                BatchPresentationTestHelper.SORT_EXECUTOR_DESCRIPTION, true), groups);
    }

    public void testGetGroupsInThatExecutorNotPresent_CriteriaSortDescriptionDesc() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 2 }, new boolean[] { false });
        List<Group> groups = executorService.getExecutorGroups(helper.getViewerSubject(), helper.getSubjectedActor(), batchPresentation, true);
        WebArrayAssert.assertEqualArrays("executor names arrays are not equals", helper.sortExecutorsArray(helper.getTestGroups(),
                BatchPresentationTestHelper.SORT_EXECUTOR_DESCRIPTION, false), groups);
    }

    public void testGetGroupsInThatExecutorNotPresent_CriteriaSortNameAsc() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 0 }, new boolean[] { true });
        List<Group> groups = executorService.getExecutorGroups(helper.getViewerSubject(), helper.getSubjectedActor(), batchPresentation, true);
        WebArrayAssert.assertEqualArrays("executor names arrays are not equals", helper.sortExecutorsArray(helper.getTestGroups(),
                BatchPresentationTestHelper.SORT_EXECUTOR_NAME, true), groups);
    }

    public void testGetGroupsInThatExecutorNotPresent_CriteriaSortNameDesc() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 0 }, new boolean[] { false });
        List<Group> groups = executorService.getExecutorGroups(helper.getViewerSubject(), helper.getSubjectedActor(), batchPresentation, true);
        WebArrayAssert.assertEqualArrays("executor names arrays are not equals", helper.sortExecutorsArray(helper.getTestGroups(),
                BatchPresentationTestHelper.SORT_EXECUTOR_NAME, false), groups);
    }

    public void testGetGroupsInThatExecutorNotPresent_CriteriaSortNameDescriptionAsc() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 0, 2 }, new boolean[] { true, true });
        List<Group> groups = executorService.getExecutorGroups(helper.getViewerSubject(), helper.getSubjectedActor(), batchPresentation, true);
        WebArrayAssert.assertEqualArrays("executor names arrays are not equals", helper.sortExecutorsArray(helper.getTestGroups(),
                BatchPresentationTestHelper.SORT_EXECUTOR_NAME_DESCRIPTION, true), groups);
    }

    public void testGetGroupsInThatExecutorNotPresent_CriteriaSortNameDescriptionDesc() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 0, 2 }, new boolean[] { false, false });
        List<Group> groups = executorService.getExecutorGroups(helper.getViewerSubject(), helper.getSubjectedActor(), batchPresentation, true);
        WebArrayAssert.assertEqualArrays("group names arrays are not equals", helper.sortExecutorsArray(helper.getTestGroups(),
                BatchPresentationTestHelper.SORT_EXECUTOR_NAME_DESCRIPTION, false), groups);
    }
}
