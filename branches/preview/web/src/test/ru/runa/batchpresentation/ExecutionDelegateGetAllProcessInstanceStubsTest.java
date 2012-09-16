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
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.service.ExecutionService;

/**
 * Created on 20.05.2005
 * 
 */
public class ExecutionDelegateGetAllProcessInstanceStubsTest extends ServletTestCase {
    private ExecutionService executionService;

    private BatchPresentation batchPresentation;

    private BatchPresentationTestHelper helper;

    public static Test suite() {
        return new TestSuite(ExecutionDelegateGetAllProcessInstanceStubsTest.class);
    }

    protected void setUp() throws Exception {
        executionService = DelegateFactory.getInstance().getExecutionService();

        helper = new BatchPresentationTestHelper(getClass().getName());
        helper.createViewerExecutor();

        batchPresentation = helper.getProcessInstanceBatchPresentation();

        helper.deployProcessInstances();
        helper.startProcessInstances();

        super.setUp();
    }

    protected void tearDown() throws Exception {
        helper.releaseResources();
        executionService = null;
        super.tearDown();
    }

    public void testGetAllProcessInstanceStubs_FilterId() throws Exception {
        int i = 2;
        int[] filteredFields = new int[] { 0 };
        Map<Integer, FilterCriteria> filteredTemplates = helper.adaptFilterFields(filteredFields, new String[] { String.valueOf(helper
                .getProcessInstanceIds()[i]) }, batchPresentation);
        batchPresentation.setFilteredFieldsMap(filteredTemplates);
        List<ProcessInstanceStub> instances = executionService.getProcessInstanceStubs(helper.getViewerSubject(), batchPresentation);
        assertEquals("array length differs from expected", 1, instances.size());
        assertEquals("process instances count differs from expected", helper.getProcessInstances().get(i), instances.get(0));
    }

    public void testGetAllProcessInstanceStubCount_FilterId() throws Exception {
        int i = 2;
        int[] filteredFields = { 0 };
        Map<Integer, FilterCriteria> filteredTemplates = helper.adaptFilterFields(filteredFields, new String[] { String.valueOf(helper
                .getProcessInstanceIds()[i]) }, batchPresentation);
        batchPresentation.setFilteredFieldsMap(filteredTemplates);
        int actual = executionService.getAllProcessInstanceStubsCount(helper.getViewerSubject(), batchPresentation);
        assertEquals("process instances count differs from expected", 1, actual);
    }

    public void testGetAllProcessInstanceStubCount_FilterName() throws Exception {
        int i = 2;
        int[] filteredFields = { 1 };
        Map<Integer, FilterCriteria> filteredTemplates = helper.adaptFilterFields(filteredFields, new String[] { helper.getProcessInstances().get(i)
                .getName() }, batchPresentation);
        batchPresentation.setFilteredFieldsMap(filteredTemplates);
        int actual = executionService.getAllProcessInstanceStubsCount(helper.getViewerSubject(), batchPresentation);
        assertEquals("process instances count differs from expected", 1, actual);
    }

    public void testGetAllProcessInstanceStubs_SortNameAsc() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 1 }, new boolean[] { true });
        List<ProcessInstanceStub> instances = executionService.getProcessInstanceStubs(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertEqualArrays("process instance entities arrays are not equals", helper.sortProcessInstanceArray(
                helper.getProcessInstances(), BatchPresentationTestHelper.SORT_PROCESS_INSTANCE_NAME, true), instances);
    }

    public void testGetAllProcessInstanceStubs_SortNameDesc() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 1 }, new boolean[] { false });
        List<ProcessInstanceStub> instances = executionService.getProcessInstanceStubs(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertEqualArrays("process instance entities arrays are not equals", helper.sortProcessInstanceArray(
                helper.getProcessInstances(), BatchPresentationTestHelper.SORT_PROCESS_INSTANCE_NAME, false), instances);
    }

    public void testGetAllProcessInstanceStubs_SortIdAsc() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 0 }, new boolean[] { true });
        List<ProcessInstanceStub> instances = executionService.getProcessInstanceStubs(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertEqualArrays("process instance entities arrays are not equals", helper.sortProcessInstanceArray(
                helper.getProcessInstances(), BatchPresentationTestHelper.SORT_PROCESS_INSTANCE_ID, true), instances);
    }

    public void testGetAllProcessInstanceStubs_SortIdDesc() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 0 }, new boolean[] { false });
        List<ProcessInstanceStub> instances = executionService.getProcessInstanceStubs(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertEqualArrays("process instance entities arrays are not equals", helper.sortProcessInstanceArray(
                helper.getProcessInstances(), BatchPresentationTestHelper.SORT_PROCESS_INSTANCE_ID, false), instances);
    }

    public void testGetAllProcessInstanceStubs_SortStartDate() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 2 }, new boolean[] { false });
        List<ProcessInstanceStub> instances = executionService.getProcessInstanceStubs(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertEqualArrays("process instance entities arrays are not equals", helper.sortProcessInstanceArray(
                helper.getProcessInstances(), BatchPresentationTestHelper.SORT_PROCESS_INSTANCE_START_DATE, false), instances);

        batchPresentation.setFieldsToSort(new int[] { 2 }, new boolean[] { true });
        instances = executionService.getProcessInstanceStubs(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertEqualArrays("process instance entities arrays are not equals", helper.sortProcessInstanceArray(
                helper.getProcessInstances(), BatchPresentationTestHelper.SORT_PROCESS_INSTANCE_START_DATE, true), instances);
    }

    public void testGetAllProcessInstanceStubs_SortEndDate() throws Exception {
        helper.endProcessInstances();
        batchPresentation.setFieldsToSort(new int[] { 3 }, new boolean[] { false });
        List<ProcessInstanceStub> instances = executionService.getProcessInstanceStubs(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertEqualArrays("process instance entities arrays are not equals", helper.sortProcessInstanceArray(
                helper.getProcessInstances(), BatchPresentationTestHelper.SORT_PROCESS_INSTANCE_END_DATE, false), instances);

        batchPresentation.setFieldsToSort(new int[] { 3 }, new boolean[] { true });
        instances = executionService.getProcessInstanceStubs(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertEqualArrays("process instance entities arrays are not equals", helper.sortProcessInstanceArray(
                helper.getProcessInstances(), BatchPresentationTestHelper.SORT_PROCESS_INSTANCE_END_DATE, true), instances);
    }
}
