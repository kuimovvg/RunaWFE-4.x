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
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.service.DefinitionService;

/**
 * Created on 20.05.2005
 * 
 */
public class DefinitionDelegateGetLatestProcessDefinitionStubsTest extends ServletTestCase {
    private DefinitionService definitionService;

    private BatchPresentation batchPresentation;

    private BatchPresentationTestHelper helper;

    public static Test suite() {
        return new TestSuite(DefinitionDelegateGetLatestProcessDefinitionStubsTest.class);
    }

    protected void setUp() throws Exception {
        definitionService = DelegateFactory.getInstance().getDefinitionService();

        helper = new BatchPresentationTestHelper(getClass().getName());
        helper.createViewerExecutor();

        batchPresentation = helper.getProcessDefinitionBatchPresentation();

        helper.deployProcessInstances();
        helper.redeployProcessInstances();

        super.setUp();
    }

    protected void tearDown() throws Exception {
        helper.releaseResources();
        definitionService = null;
        super.tearDown();
    }

    public void testGetLatestProcessDefinitionStubs_FilterName() throws Exception {
        int i = 0;
        int[] filteredFields = new int[] { 0 };
        Map<Integer, FilterCriteria> filteredTemplates = helper.adaptFilterFields(filteredFields,
                new String[] { helper.getProcessDefinitionNames()[i] }, batchPresentation);
        batchPresentation.setFilteredFieldsMap(filteredTemplates);
        List<ProcessDefinition> definitions = definitionService.getLatestProcessDefinitionStubs(helper.getViewerSubject(), batchPresentation);
        assertEquals("array length differs from expected", 1, definitions.size());
        assertEquals("process definitions differs from expected", helper.getProcessDefinitions().get(i), definitions.get(0));
    }

    public void testGetLatestProcessDefinitionStubs_FilterMultipleNames() throws Exception {
        String namePattern = "%i_s%";
        int[] filteredFields = new int[] { 0 };
        Map<Integer, FilterCriteria> filteredTemplates = helper.adaptFilterFields(filteredFields, new String[] { namePattern }, batchPresentation);
        batchPresentation.setFilteredFieldsMap(filteredTemplates);
        List<ProcessDefinition> definitions = definitionService.getLatestProcessDefinitionStubs(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertWeakEqualArrays("process definitions differs from expected", helper.getProcessDefinitionsWithNamePattern(namePattern),
                definitions);
    }

    public void testGetLatestProcessDefinitionStubs_FilterMultipleNames2() throws Exception {
        String namePattern = "_i_s_";
        int[] filteredFields = new int[] { 0 };
        Map<Integer, FilterCriteria> filteredTemplates = helper.adaptFilterFields(filteredFields, new String[] { namePattern }, batchPresentation);
        batchPresentation.setFilteredFieldsMap(filteredTemplates);
        List<ProcessDefinition> definitions = definitionService.getLatestProcessDefinitionStubs(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertWeakEqualArrays("process definitions differs from expected", helper.getProcessDefinitionsWithNamePattern(namePattern),
                definitions);
    }

    public void testGetLatestProcessDefinitionStubs_FilterNameDescriptionVersionTheSameDefinition() throws Exception {
        int i = 2;
        int[] filteredFields = new int[] { 0, 1 };
        Map<Integer, FilterCriteria> filteredTemplates = helper.adaptFilterFields(filteredFields, new String[] {
                helper.getProcessDefinitionNames()[i], helper.getProcessDefinitionDescriptions()[i] }, batchPresentation);
        batchPresentation.setFilteredFieldsMap(filteredTemplates);
        List<ProcessDefinition> definitions = definitionService.getLatestProcessDefinitionStubs(helper.getViewerSubject(), batchPresentation);
        assertEquals("array length differs from expected", 1, definitions.size());
        assertEquals("process definitions differs from expected", helper.getProcessDefinitions().get(i), definitions.get(0));
    }

    public void testGetLatestProcessDefinitionStubs_SortNameAsc() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 0 }, new boolean[] { true });
        List<ProcessDefinition> definitions = definitionService.getLatestProcessDefinitionStubs(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertEqualArrays("process definitions entities arrays are not equals", helper.sortProcessDefinitionArray(helper
                .getProcessDefinitions(), BatchPresentationTestHelper.SORT_PROCESS_DEFINITION_NAME, true), definitions);
    }

    public void testGetLatestProcessDefinitionStubs_SortDescriptionAsc() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 1 }, new boolean[] { true });
        List<ProcessDefinition> definitions = definitionService.getLatestProcessDefinitionStubs(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertEqualArrays("process definitions entities arrays are not equals", helper.sortProcessDefinitionArray(helper
                .getProcessDefinitions(), BatchPresentationTestHelper.SORT_PROCESS_DEFINITION_DESCRIPTION, true), definitions);
    }

    public void testGetLatestProcessDefinitionStubs_SortNameDesc() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 0 }, new boolean[] { false });
        List<ProcessDefinition> definitions = definitionService.getLatestProcessDefinitionStubs(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertEqualArrays("process definitions entities arrays are not equals", helper.sortProcessDefinitionArray(helper
                .getProcessDefinitions(), BatchPresentationTestHelper.SORT_PROCESS_DEFINITION_NAME, false), definitions);
    }

    public void testGetLatestProcessDefinitionStubs_SortDescriptionDesc() throws Exception {
        batchPresentation.setFieldsToSort(new int[] { 1 }, new boolean[] { false });
        List<ProcessDefinition> definitions = definitionService.getLatestProcessDefinitionStubs(helper.getViewerSubject(), batchPresentation);
        WebArrayAssert.assertEqualArrays("process definitions entities arrays are not equals", helper.sortProcessDefinitionArray(helper
                .getProcessDefinitions(), BatchPresentationTestHelper.SORT_PROCESS_DEFINITION_DESCRIPTION, false), definitions);
    }
}
