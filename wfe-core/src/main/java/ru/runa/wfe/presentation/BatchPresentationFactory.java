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
package ru.runa.wfe.presentation;

import java.util.Map;

import ru.runa.wfe.audit.SystemLogClassPresentation;
import ru.runa.wfe.definition.DefinitionClassPresentation;
import ru.runa.wfe.execution.ProcessClassPresentation;
import ru.runa.wfe.presentation.filter.FilterCriteria;
import ru.runa.wfe.relation.RelationClassPresentation;
import ru.runa.wfe.relation.RelationGroupClassPresentation;
import ru.runa.wfe.task.TaskClassPresentation;
import ru.runa.wfe.user.ExecutorClassPresentation;

import com.google.common.collect.Maps;

/**
 * Default batch presentation factory.
 * 
 * @author Dofs
 * @since 4.0
 */
public class BatchPresentationFactory {
    public static final BatchPresentationFactory RELATION_GROUPS = new BatchPresentationFactory(RelationGroupClassPresentation.class);
    public static final BatchPresentationFactory RELATIONS = new BatchPresentationFactory(RelationClassPresentation.class);
    public static final BatchPresentationFactory EXECUTORS = new BatchPresentationFactory(ExecutorClassPresentation.class, 100);
    public static final BatchPresentationFactory SYSTEM_LOGS = new BatchPresentationFactory(SystemLogClassPresentation.class);
    public static final BatchPresentationFactory PROCESSES = new BatchPresentationFactory(ProcessClassPresentation.class);
    public static final BatchPresentationFactory DEFINITIONS = new BatchPresentationFactory(DefinitionClassPresentation.class);
    public static final BatchPresentationFactory TASKS = new BatchPresentationFactory(TaskClassPresentation.class);

    private final int[] fieldsToSortIds = {};
    private final boolean[] fieldsToSortModes = {};
    private final int[] fieldsToGroupIds = {};
    private final int[] fieldsToDisplayIds;
    private final ClassPresentation classPresentation;
    private final int defaultPageRangeSize;

    public BatchPresentationFactory(Class<?> classPresentationClass) {
        this(classPresentationClass, BatchPresentationConsts.getAllowedViewSizes()[0]);
    }

    public BatchPresentationFactory(Class<?> classPresentationClass, int defaultPageRangeSize) {
        this.classPresentation = ClassPresentations.getClassPresentation(classPresentationClass);
        this.defaultPageRangeSize = defaultPageRangeSize;
        // if (CoreResources.isInitialDisplayFieldsEmpty()) {
        // // This case is for RTN - do not create dependence from jbpm.core.jar
        // fieldsToDisplayIds = new int[] {};
        // } else {
        int displayedFieldsCount = classPresentation.getFields().length;
        for (FieldDescriptor field : classPresentation.getFields()) {
            if (field.displayName.startsWith(ClassPresentation.editable_prefix)) {
                --displayedFieldsCount;
            }
        }
        fieldsToDisplayIds = new int[displayedFieldsCount];
        for (int i = classPresentation.getFields().length - 1; i >= 0; i--) {
            if (classPresentation.getFields()[i].displayName.startsWith(ClassPresentation.editable_prefix)) {
                continue;
            }
            fieldsToDisplayIds[--displayedFieldsCount] = i;
        }
        // }
    }

    public BatchPresentation createDefault() {
        return createDefault(BatchPresentationConsts.DEFAULT_ID);
    }

    public BatchPresentation createDefault(String batchPresentationId) {
        BatchPresentation result = new BatchPresentation(BatchPresentationConsts.DEFAULT_NAME, batchPresentationId, classPresentation,
                getFieldsToSortIds(), getFieldsToSortModes(), getFieldsToDisplayIds(), getFieldsToFilter(), getFieldsToGroupIds());
        result.setRangeSize(defaultPageRangeSize);
        return result;
    }

    private int[] getFieldsToDisplayIds() {
        return fieldsToDisplayIds.clone();
    }

    private int[] getFieldsToGroupIds() {
        return fieldsToGroupIds.clone();
    }

    private int[] getFieldsToSortIds() {
        return fieldsToSortIds.clone();
    }

    private boolean[] getFieldsToSortModes() {
        return fieldsToSortModes.clone();
    }

    private Map<Integer, FilterCriteria> getFieldsToFilter() {
        return Maps.newHashMap();
    }
}
