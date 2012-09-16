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

import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.filter.FilterCriteria;
import ru.runa.delegate.DelegateFactory;
import ru.runa.junit.WebArrayAssert;
import ru.runa.wf.TaskStub;
import ru.runa.wf.service.ExecutionService;

/**
 * Created on 20.05.2005
 *
 *
 */
public class ExecutionDelegateGetTasksTest extends ServletTestCase {
    private ExecutionService executionService;
    private BatchPresentation batchPresentation;

    private BatchPresentationTestHelper helper;

    public static Test suite() {
        return new TestSuite(ExecutionDelegateGetTasksTest.class);
    }

    protected void setUp() throws Exception {
        executionService = DelegateFactory.getInstance().getExecutionService();

        helper = new BatchPresentationTestHelper(getClass().getName());
        helper.createViewerExecutor();

        batchPresentation = helper.getTaskBatchPresentation();

        helper.deployProcessInstances();
        helper.startProcessInstances();

        super.setUp();
    }

    protected void tearDown() throws Exception {
        helper.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testGetTasks_FilterStateName() throws Exception {
        int i = 2;
        List<TaskStub> expectedTasks = helper.getTasks();
        int[] filteredFields = new int[] { 0 };
        Map<Integer, FilterCriteria> filteredTemplates = helper.adaptFilterFields(filteredFields, new String[] { expectedTasks.get(i).getName() },
                batchPresentation);
        batchPresentation.setFilteredFieldsMap(filteredTemplates);
        List<TaskStub> tasks = executionService.getTasks(helper.getViewerSubject(), batchPresentation);
        assertEquals("array length differs from expected", 1, tasks.size());
        assertEquals("tasks count differs from expected", expectedTasks.get(i), tasks.get(0));
    }

    public void testGetTasks_FilterMultipleStateNames() throws Exception {
        String namePattern = "%st_te%";
        int[] filteredFields = new int[] { 0 };
        Map<Integer, FilterCriteria> filteredTemplates = helper.adaptFilterFields(filteredFields, new String[] { namePattern }, batchPresentation);
        batchPresentation.setFilteredFieldsMap(filteredTemplates);
        List<TaskStub> tasks = executionService.getTasks(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertWeakEqualArrays("tasks count differs from expected", helper.getTasksWithNamePattern(namePattern), tasks);
    }

    public void testGetTasks_FilterMultipleStateNames2() throws Exception {
        String namePattern = "%stute_";
        int[] filteredFields = new int[] { 0 };
        Map<Integer, FilterCriteria> filteredTemplates = helper.adaptFilterFields(filteredFields, new String[] { namePattern }, batchPresentation);
        batchPresentation.setFilteredFieldsMap(filteredTemplates);
        List<TaskStub> tasks = executionService.getTasks(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertWeakEqualArrays("tasks count differs from expected", helper.getTasksWithNamePattern(namePattern), tasks);
    }

    public void testGetTasks_FilterProcessId() throws Exception {
        int i = 1;
        List<TaskStub> expectedTasks = helper.getTasks();
        int[] filteredFields = new int[] { 3 };
        Map<Integer, FilterCriteria> filteredTemplates = helper.adaptFilterFields(filteredFields, new String[] { String.valueOf(expectedTasks.get(i)
                .getProcessInstanceId()) }, batchPresentation);
        batchPresentation.setFilteredFieldsMap(filteredTemplates);
        List<TaskStub> tasks = executionService.getTasks(helper.getViewerSubject(), batchPresentation);
        assertEquals("array length differs from expected", 1, tasks.size());
        assertEquals("task count differs from expected", expectedTasks.get(i), tasks.get(0));
    }

    public void testGetTasks_SortStateNameAsc() throws Exception {
        List<TaskStub> expectedTasks = helper.getTasks();

        batchPresentation.setFieldsToSort(new int[] { 0 }, new boolean[] { true });
        List<TaskStub> tasks = executionService.getTasks(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertEqualArrays("task entities arrays are not equals", helper.sortTasksArray(expectedTasks,
                BatchPresentationTestHelper.SORT_TASK_STATE_NAME, true), tasks);
    }

    public void testGetTasks_SortNameDesc() throws Exception {

        List<TaskStub> expectedTasks = helper.getTasks();

        batchPresentation.setFieldsToSort(new int[] { 0 }, new boolean[] { false });
        List<TaskStub> tasks = executionService.getTasks(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertEqualArrays("task entities arrays are not equals", helper.sortTasksArray(expectedTasks,
                BatchPresentationTestHelper.SORT_TASK_STATE_NAME, false), tasks);
    }

    public void testGetTasks_SortDescriptionAsc() throws Exception {
        List<TaskStub> expectedTasks = helper.getTasks();

        batchPresentation.setFieldsToSort(new int[] { 1 }, new boolean[] { true });
        List<TaskStub> tasks = executionService.getTasks(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertEqualArrays("task entities arrays are not equals", helper.sortTasksArray(expectedTasks,
                BatchPresentationTestHelper.SORT_TASK_STATE_DESCRIPTION, true), tasks);
    }

    public void testGetTasks_SortDescriptionDesc() throws Exception {
        List<TaskStub> expectedTasks = helper.getTasks();

        batchPresentation.setFieldsToSort(new int[] { 1 }, new boolean[] { false });
        List<TaskStub> tasks = executionService.getTasks(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertEqualArrays("task entities arrays are not equals", helper.sortTasksArray(expectedTasks,
                BatchPresentationTestHelper.SORT_TASK_STATE_DESCRIPTION, false), tasks);
    }

    public void testGetTasks_SortDefinitionNameDesc() throws Exception {
        List<TaskStub> expectedTasks = helper.getTasks();

        batchPresentation.setFieldsToSort(new int[] { 2 }, new boolean[] { false });
        List<TaskStub> tasks = executionService.getTasks(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertEqualArrays("task entities arrays are not equals", helper.sortTasksArray(expectedTasks,
                BatchPresentationTestHelper.SORT_TASK_DEFINITION_NAME, false), tasks);
    }

    public void testGetTasks_SortDefinitionNameAsc() throws Exception {
        List<TaskStub> expectedTasks = helper.getTasks();

        batchPresentation.setFieldsToSort(new int[] { 2 }, new boolean[] { true });
        List<TaskStub> tasks = executionService.getTasks(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertEqualArrays("task entities arrays are not equals", helper.sortTasksArray(expectedTasks,
                BatchPresentationTestHelper.SORT_TASK_DEFINITION_NAME, true), tasks);
    }
}
