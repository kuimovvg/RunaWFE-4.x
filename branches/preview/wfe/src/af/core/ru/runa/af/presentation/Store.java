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

import java.util.ArrayList;
import java.util.List;

import ru.runa.commons.ArraysCommons;

/**
 * Holds field lists for {@link BatchPresentation}.
 */
class Store {

    /**
     * All fields from {@link BatchPresentation}. 
     */
    public final FieldDescriptor[] allFields;

    /**
     * Displaying fields of {@link BatchPresentation}. 
     */
    public final FieldDescriptor[] displayFields;

    /**
     * Hidden fields of {@link BatchPresentation}.
     */
    public final FieldDescriptor[] hiddenFields;

    /**
     * Sorted fields of {@link BatchPresentation}.
     */
    public final FieldDescriptor[] sortedFields;

    /**
     * Grouped fields of {@link BatchPresentation}.
     */
    public final FieldDescriptor[] groupedFields;

    /**
     * Creates storage to hold different fields for batch presentation.
     * @param batchPresentation {@link BatchPresentation}, to create field lists. 
     */
    public Store(BatchPresentation batchPresentation) {
        allFields = new FieldDescriptor[batchPresentation.getClassPresentation().getFields().length + batchPresentation.dynamicFields.size()];
        for (int idx = 0; idx < batchPresentation.dynamicFields.size(); ++idx) {
            allFields[idx] = batchPresentation.getClassPresentation().getFields()[batchPresentation.dynamicFields.get(idx).getFieldIdx().intValue()]
                    .createConcretteEditableField(batchPresentation.dynamicFields.get(idx).getDynamicValue(), idx);
        }
        for (int idx = 0; idx < batchPresentation.getClassPresentation().getFields().length; ++idx) {
            allFields[idx + batchPresentation.dynamicFields.size()] = batchPresentation.getClassPresentation().getFields()[idx]
                    .createConcretteField(idx + batchPresentation.dynamicFields.size());
        }
        displayFields = removeNotEnabled((FieldDescriptor[]) ArraysCommons.createArrayValuesByIndex(allFields, batchPresentation.fieldsToDisplayIds));
        sortedFields = (FieldDescriptor[]) ArraysCommons.createArrayValuesByIndex(allFields, batchPresentation.fieldsToSortIds);
        groupedFields = (FieldDescriptor[]) ArraysCommons.createArrayValuesByIndex(allFields, batchPresentation.fieldsToGroupIds);
        int fieldsCount = allFields.length;
        List<Integer> fieldIdList = ArraysCommons.createArrayListFilledIncrement(fieldsCount);
        fieldIdList.removeAll(ArraysCommons.createIntegerList(batchPresentation.fieldsToDisplayIds));
        hiddenFields = (FieldDescriptor[]) ArraysCommons.createArrayValuesByIndex(allFields, ArraysCommons.createIntArray(fieldIdList));
    }

    /**
     * Removes all fields with not ENABLE state.
     * @param fields Fields, to remove not ENABLED.
     * @return ENABLED fields list.
     */
    private FieldDescriptor[] removeNotEnabled(FieldDescriptor[] fields) {
        List<FieldDescriptor> result = new ArrayList<FieldDescriptor>();
        for (FieldDescriptor fieldDescriptor : fields) {
            if (fieldDescriptor.fieldState == FieldState.ENABLED) {
                result.add(fieldDescriptor);
            }
        }
        return result.toArray(new FieldDescriptor[result.size()]);
    }
}
