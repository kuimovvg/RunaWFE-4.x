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
package ru.runa.af.presentation.filter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ru.runa.InternalApplicationException;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.bpm.context.exe.VariableInstance;
import ru.runa.bpm.context.exe.variableinstance.ByteArrayInstance;
import ru.runa.bpm.context.exe.variableinstance.DateInstance;
import ru.runa.bpm.context.exe.variableinstance.DoubleInstance;
import ru.runa.bpm.context.exe.variableinstance.LongInstance;
import ru.runa.bpm.context.exe.variableinstance.StringInstance;
import ru.runa.commons.Loader;

/**
 * 
 * Created on 12.02.2007
 * 
 */
public class FilterCriteriaFactory {

    private static Map<String, FilterCriteria> filterCriterias = new HashMap<String, FilterCriteria>();

    static {
        filterCriterias.put(String.class.getName(), new StringFilterCriteria());
        filterCriterias.put(Integer.class.getName(), new IntegerFilterCriteria());
        filterCriterias.put(VariableInstance.class.getName(), new AnywhereStringFilterCriteria());
        filterCriterias.put(ByteArrayInstance.class.getName(), new AnywhereStringFilterCriteria());
        filterCriterias.put(DateInstance.class.getName(), new AnywhereStringFilterCriteria());
        filterCriterias.put(DoubleInstance.class.getName(), new AnywhereStringFilterCriteria());
        filterCriterias.put(LongInstance.class.getName(), new AnywhereStringFilterCriteria());
        filterCriterias.put(StringInstance.class.getName(), new AnywhereStringFilterCriteria());
        filterCriterias.put(Date.class.getName(), new DateFilterCriteria());
    }

    public static FilterCriteria getFilterCriteria(BatchPresentation batchPresentation, int fieldId) {
        String fieldType = batchPresentation.getAllFields()[fieldId].fieldType;
        return getFilterCriteria(fieldType);
    }

    public static FilterCriteria getFilterCriteria(String fieldType) {
        FilterCriteria filter = filterCriterias.get(fieldType);
        if (filter != null) {
            return filter.clone();
        }
        try {
            return (FilterCriteria) Loader.loadObject(fieldType, null);
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }
}
