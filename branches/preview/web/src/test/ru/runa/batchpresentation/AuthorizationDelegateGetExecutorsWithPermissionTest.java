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
import ru.runa.af.presentation.filter.FilterCriteria;
import ru.runa.af.service.AuthorizationService;
import ru.runa.delegate.DelegateFactory;
import ru.runa.junit.WebArrayAssert;

/**
 * Created on 20.05.2005
 * 
 */
public class AuthorizationDelegateGetExecutorsWithPermissionTest extends ServletTestCase {
    private AuthorizationService authorizationService;

    private BatchPresentation batchPresentation;

    private BatchPresentationTestHelper helper;

    public static Test suite() {
        return new TestSuite(AuthorizationDelegateGetExecutorsWithPermissionTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        authorizationService = DelegateFactory.getInstance().getAuthorizationService();

        helper = new BatchPresentationTestHelper(getClass().getName());
        helper.createTestExecutors();
        helper.createViewerExecutor();
        helper.createSubjectedExecutor();
        helper.setREADPermissionsToViewerActorOnTestExecutors();
        helper.setPermissionToTestExecutorsOnSubjectedActor();

        batchPresentation = helper.getExecutorBatchPresentation();

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.releaseResources();
        authorizationService = null;
        super.tearDown();
    }

    public void testGetExecutorsWithPermission_CriteriaFilterName() throws Exception {
        int i = 4;
        int[] filteredFields = new int[] { 0 };
        Map<Integer, FilterCriteria> filteredTemplates = helper.adaptFilterFields(filteredFields, new String[] { helper.getTestExecutorsNames()[i] },
                batchPresentation);
        batchPresentation.setFilteredFieldsMap(filteredTemplates);
        List<Executor> executors = authorizationService
                .getExecutorsWithPermission(helper.getViewerSubject(), helper.getSubjectedActor(), batchPresentation, true);
        assertEquals("array length differs from expected", 1, executors.size());
        assertEquals("executor differs from expected", helper.getTestExecutors().get(i), executors.get(0));
    }

    public void testGetExecutorsWithPermission_CriteriaFilterNameDescriptionSameExecutor() throws Exception {
        int i = 3;
        int[] filteredFields = new int[] { 0, 2 };
        Map<Integer, FilterCriteria> filteredTemplates = helper.adaptFilterFields(filteredFields, new String[] { helper.getTestExecutorsNames()[i],
                helper.getTestExecutorsDescriptions()[i] }, batchPresentation);
        batchPresentation.setFilteredFieldsMap(filteredTemplates);
        List<Executor> executors = authorizationService
                .getExecutorsWithPermission(helper.getViewerSubject(), helper.getSubjectedActor(), batchPresentation, true);
        assertEquals("array length differs from expected", 1, executors.size());
        assertEquals("executor differs from expected", helper.getTestExecutors().get(i), executors.get(0));
    }

    public void testGetExecutorsWithPermission_CriteriaFilterNameDescriptionDifferExecutor() throws Exception {
        int i = 2;
        int[] filteredFields = new int[] { 0, 2 };
        Map<Integer, FilterCriteria> filteredTemplates = helper.adaptFilterFields(filteredFields, new String[] { helper.getTestExecutorsNames()[i],
                helper.getTestExecutorsDescriptions()[i + 1] }, batchPresentation);
        batchPresentation.setFilteredFieldsMap(filteredTemplates);
        List<Executor> executors = authorizationService
                .getExecutorsWithPermission(helper.getViewerSubject(), helper.getSubjectedActor(), batchPresentation, true);
        assertEquals("array length differs from expected", 0, executors.size());
    }

    public void testGetExecutorsWithPermission_CriteriaSortDescriptionAsc() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 2 }, new boolean[] { true });
        List<Executor> executors = authorizationService
                .getExecutorsWithPermission(helper.getViewerSubject(), helper.getSubjectedActor(), batchPresentation, true);
        WebArrayAssert.assertEqualArrays("executor names arrays are not equals", helper.sortExecutorsArray(helper
                .getTestExecutorsWithSubjectedAndViewerActors(), BatchPresentationTestHelper.SORT_EXECUTOR_DESCRIPTION, true), executors);
    }

    public void testGetExecutorsWithPermission_CriteriaSortDescriptionDesc() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 2 }, new boolean[] { false });
        List<Executor> executors = authorizationService
                .getExecutorsWithPermission(helper.getViewerSubject(), helper.getSubjectedActor(), batchPresentation, true);
        WebArrayAssert.assertEqualArrays("executor names arrays are not equals", helper.sortExecutorsArray(helper
                .getTestExecutorsWithSubjectedAndViewerActors(), BatchPresentationTestHelper.SORT_EXECUTOR_DESCRIPTION, false), executors);
    }

    public void testGetExecutorsWithPermission_CriteriaSortNameAsc() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 0 }, new boolean[] { true });
        List<Executor> executors = authorizationService
                .getExecutorsWithPermission(helper.getViewerSubject(), helper.getSubjectedActor(), batchPresentation, true);
        WebArrayAssert.assertEqualArrays("executor names arrays are not equals", helper.sortExecutorsArray(helper
                .getTestExecutorsWithSubjectedAndViewerActors(), BatchPresentationTestHelper.SORT_EXECUTOR_NAME, true), executors);
    }

    public void testGetExecutorsWithPermission_CriteriaSortNameDesc() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 0 }, new boolean[] { false });
        List<Executor> executors = authorizationService
                .getExecutorsWithPermission(helper.getViewerSubject(), helper.getSubjectedActor(), batchPresentation, true);
        WebArrayAssert.assertEqualArrays("executor names arrays are not equals", helper.sortExecutorsArray(helper
                .getTestExecutorsWithSubjectedAndViewerActors(), BatchPresentationTestHelper.SORT_EXECUTOR_NAME, false), executors);
    }

    public void testGetExecutorsWithPermission_CriteriaSortNameDescriptionAsc() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 0, 2 }, new boolean[] { true, true });
        List<Executor> executors = authorizationService
                .getExecutorsWithPermission(helper.getViewerSubject(), helper.getSubjectedActor(), batchPresentation, true);
        WebArrayAssert.assertEqualArrays("executor names arrays are not equals", helper.sortExecutorsArray(helper
                .getTestExecutorsWithSubjectedAndViewerActors(), BatchPresentationTestHelper.SORT_EXECUTOR_NAME_DESCRIPTION, true), executors);
    }

    public void testGetExecutorsWithPermission_CriteriaSortNameDescriptionDesc() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 0, 2 }, new boolean[] { false, false });
        List<Executor> executors = authorizationService
                .getExecutorsWithPermission(helper.getViewerSubject(), helper.getSubjectedActor(), batchPresentation, true);
        WebArrayAssert.assertEqualArrays("executor names arrays are not equals", helper.sortExecutorsArray(helper
                .getTestExecutorsWithSubjectedAndViewerActors(), BatchPresentationTestHelper.SORT_EXECUTOR_NAME_DESCRIPTION, false), executors);
    }
}
