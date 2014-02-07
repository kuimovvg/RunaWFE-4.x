package ru.runa.wfe.office.doc;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.LongFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class AbstractIteratorOperation extends Operation {
    protected IterateBy iterateBy;
    protected String containerVariableName;
    protected WfVariable containerVariable;

    public IterateBy getIterateBy() {
        return iterateBy;
    }

    public void setIterateBy(IterateBy iterateBy) {
        this.iterateBy = iterateBy;
    }

    public String getContainerVariableName() {
        return containerVariableName;
    }

    public void setContainerVariableName(String containerName) {
        this.containerVariableName = containerName;
    }

    @Override
    public String getName() {
        return iterateBy.name();
    }

    @Override
    public boolean isValid() {
        return iterateBy != null && containerVariableName != null && containerVariable != null
                && (getContainerValue() instanceof Map || getContainerValue() instanceof List);
    }

    public Object getContainerValue() {
        if (containerVariable == null) {
            return null;
        }
        return containerVariable.getValue();
    }

    public void setContainerVariable(WfVariable containerVariable) {
        this.containerVariable = containerVariable;
    }

    public Iterator<? extends Object> createIterator() {
        if (iterateBy == IterateBy.indexes) {
            List<?> list = (List<?>) getContainerValue();
            List<Long> indexes = Lists.newArrayListWithExpectedSize(list.size());
            for (int i = 0; i < list.size(); i++) {
                indexes.add(new Long(i));
            }
            return indexes.iterator();
        }
        if (iterateBy == IterateBy.keys) {
            return ((Map<?, ?>) getContainerValue()).keySet().iterator();
        }
        if (iterateBy == IterateBy.items) {
            return ((List<?>) getContainerValue()).iterator();
        }
        if (iterateBy == IterateBy.values) {
            return ((Map<?, ?>) getContainerValue()).values().iterator();
        }
        return null;
    }

    public String getIteratorFormatClassName() {
        if (iterateBy == IterateBy.indexes) {
            return LongFormat.class.getName();
        }
        if (iterateBy == IterateBy.keys) {
            return ((VariableFormatContainer) containerVariable.getDefinition().getFormatNotNull()).getComponentClassName(0);
        }
        if (iterateBy == IterateBy.items) {
            return ((VariableFormatContainer) containerVariable.getDefinition().getFormatNotNull()).getComponentClassName(0);
        }
        if (iterateBy == IterateBy.values) {
            return ((VariableFormatContainer) containerVariable.getDefinition().getFormatNotNull()).getComponentClassName(1);
        }
        return null;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(getClass()).add("by", iterateBy).add("container", containerVariableName).add("item", iterateBy).toString();
    }
}
