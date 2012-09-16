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

import ru.runa.af.Executor;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationConsts;
import ru.runa.af.presentation.filter.FilterCriteria;
import ru.runa.af.service.ExecutorService;
import ru.runa.delegate.DelegateFactory;
import ru.runa.junit.WebArrayAssert;

/**
 * Created on 20.05.2005
 * 
 */
public class ExecutorDelegateGetAllTest extends ServletTestCase {
    private ExecutorService executorService;

    private BatchPresentation batchPresentation;

    private BatchPresentationTestHelper helper;

    public static Test suite() {
        return new TestSuite(ExecutorDelegateGetAllTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        executorService = DelegateFactory.getInstance().getExecutorService();

        helper = new BatchPresentationTestHelper(getClass().getName());
        helper.createTestExecutors();
        helper.createViewerExecutor();
        helper.createSubjectedExecutor();
        helper.setREADPermissionsToViewerActorOnTestExecutors();
        helper.setPermissionToTestExecutorsOnSubjectedActor();

        batchPresentation = helper.getExecutorBatchPresentation();
        batchPresentation.setRangeSize(BatchPresentationConsts.MAX_UNPAGED_REQUEST_SIZE);

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.releaseResources();
        executorService = null;
        super.tearDown();
    }

    public void testGetAll_CriteriaFilterName() throws Exception {
        int i = 2;
        int[] filteredFields = new int[] { 0 };
        Map<Integer, FilterCriteria> filteredTemplates = helper.adaptFilterFields(filteredFields, new String[] { helper.getTestExecutorsNames()[i] },
                batchPresentation);
        batchPresentation.setFilteredFieldsMap(filteredTemplates);
        List<Executor> executors = executorService.getAll(helper.getViewerSubject(), batchPresentation);
        assertEquals("array length differs from expected", 1, executors.size());
        assertEquals("executor differs from expected", helper.getTestExecutorsWithSubjectedAndViewerActors().get(i), executors.get(0));
    }

    public void testGetAll_CriteriaFilterMultipleName() throws Exception {
        String namePattern = "%ada%";
        int[] filteredFields = new int[] { 0 };
        Map<Integer, FilterCriteria> filteredTemplates = helper.adaptFilterFields(filteredFields, new String[] { namePattern }, batchPresentation);
        batchPresentation.setFilteredFieldsMap(filteredTemplates);
        List<Executor> executors = executorService.getAll(helper.getViewerSubject(), batchPresentation);
        List<Executor> expectedExecutors = helper.getTestExecutorsWithNamePattern(namePattern);
        WebArrayAssert.assertWeakEqualArrays("executor differs from expected", expectedExecutors, executors);
    }

    public void testGetAll_CriteriaFilterMultipleName2() throws Exception {
        String namePattern = "_ada%";
        int[] filteredFields = new int[] { 0 };
        Map<Integer, FilterCriteria> filteredTemplates = helper.adaptFilterFields(filteredFields, new String[] { namePattern }, batchPresentation);
        batchPresentation.setFilteredFieldsMap(filteredTemplates);
        List<Executor> executors = executorService.getAll(helper.getViewerSubject(), batchPresentation);
        List<Executor> expectedExecutors = helper.getTestExecutorsWithNamePattern(namePattern);
        WebArrayAssert.assertWeakEqualArrays("executor differs from expected", expectedExecutors, executors);
    }

    public void testGetAll_CriteriaSortDescriptionAsc() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 2 }, new boolean[] { true });
        List<Executor> executors = executorService.getAll(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertEqualArrays("executor names arrays are not equals", helper.sortExecutorsArray(helper
                .getTestExecutorsWithSubjectedAndViewerActors(), BatchPresentationTestHelper.SORT_EXECUTOR_DESCRIPTION, true), executors);
    }

    public void testGetAll_CriteriaSortDescriptionDesc() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 2 }, new boolean[] { false });
        List<Executor> executors = executorService.getAll(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertEqualArrays("executor names arrays are not equals", helper.sortExecutorsArray(helper
                .getTestExecutorsWithSubjectedAndViewerActors(), BatchPresentationTestHelper.SORT_EXECUTOR_DESCRIPTION, false), executors);
    }

    public void testGetAll_CriteriaSortNameAsc() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 0 }, new boolean[] { true });
        List<Executor> executors = executorService.getAll(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertEqualArrays("executor names arrays are not equals", helper.sortExecutorsArray(helper
                .getTestExecutorsWithSubjectedAndViewerActors(), BatchPresentationTestHelper.SORT_EXECUTOR_NAME, true), executors);
    }

    public void testGetAll_CriteriaSortNameDesc() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 0 }, new boolean[] { false });
        List<Executor> executors = executorService.getAll(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertEqualArrays("executor names arrays are not equals", helper.sortExecutorsArray(helper
                .getTestExecutorsWithSubjectedAndViewerActors(), BatchPresentationTestHelper.SORT_EXECUTOR_NAME, false), executors);
    }

    public void testGetAll_CriteriaSortNameDescriptionAsc() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 0, 2 }, new boolean[] { true, true });
        List<Executor> executors = executorService.getAll(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertEqualArrays("executor names arrays are not equals", helper.sortExecutorsArray(helper
                .getTestExecutorsWithSubjectedAndViewerActors(), BatchPresentationTestHelper.SORT_EXECUTOR_NAME_DESCRIPTION, true), executors);
    }

    public void testGetAll_CriteriaSortNameDescriptionDesc() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 0, 2 }, new boolean[] { false, false });
        List<Executor> executors = executorService.getAll(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertEqualArrays("executor names arrays are not equals", helper.sortExecutorsArray(helper
                .getTestExecutorsWithSubjectedAndViewerActors(), BatchPresentationTestHelper.SORT_EXECUTOR_NAME_DESCRIPTION, false), executors);
    }
}
