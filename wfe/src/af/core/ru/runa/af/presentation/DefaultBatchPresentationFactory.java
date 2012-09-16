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
package ru.runa.af.presentation;

import java.util.HashMap;
import java.util.Map;

import ru.runa.af.CoreResources;

/**
 * Created on 22.10.2005
 * 
 */
public class DefaultBatchPresentationFactory {

    private final int[] fieldsToSortIds = {};

    private final boolean[] fieldsToSortModes = {};

    private final int[] fieldsToGroupIds = {};

    private final int[] fieldsToDisplayIds;

    private final int classPresentationId;

    private final int defaultPageRangeSize;

    public DefaultBatchPresentationFactory(int classPresentationId) {
        this(classPresentationId, BatchPresentationConsts.getAllowedViewSizes()[0]);
    }

    public DefaultBatchPresentationFactory(int classPresentationId, int defaultPageRangeSize) {
        this.classPresentationId = classPresentationId;
        this.defaultPageRangeSize = defaultPageRangeSize;
        if (CoreResources.isInitialDisplayFieldsEmpty()) {
            // This case is for RTN - do not create dependence from jbpm.core.jar
            fieldsToDisplayIds = new int[] {};
        } else {
            int displayedFieldsCount = getClassPresentation().getFields().length;
            for (FieldDescriptor field : getClassPresentation().getFields()) {
                if (field.displayName.startsWith(ClassPresentation.editable_prefix)) {
                    --displayedFieldsCount;
                }
            }
            fieldsToDisplayIds = new int[displayedFieldsCount];
            for (int i = getClassPresentation().getFields().length - 1; i >= 0; i--) {
                if (getClassPresentation().getFields()[i].displayName.startsWith(ClassPresentation.editable_prefix)) {
                    continue;
                }
                fieldsToDisplayIds[--displayedFieldsCount] = i;
            }
        }
    }

    public BatchPresentation getDefaultBatchPresentation() {
        return getDefaultBatchPresentation(BatchPresentationConsts.DEFAULT_NAME, BatchPresentationConsts.DEFAULT_ID);
    }

    public BatchPresentation getDefaultBatchPresentation(String name, String batchPresentationId) {
        BatchPresentation result = new BatchPresentation(name, batchPresentationId, classPresentationId, getFieldsToSortIds(),
                getFieldsToSortModes(), getFieldsToDisplayIds(), getFieldsToFilter(), getFieldsToGroupIds());
        result.setRangeSize(defaultPageRangeSize);
        return result;
    }

    private ClassPresentation getClassPresentation() {
        return ClassPresentationFinderFactory.getClassPresentationFinder().getClassPresentationById(classPresentationId);
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

    private Map getFieldsToFilter() {
        return new HashMap();
    }
}
