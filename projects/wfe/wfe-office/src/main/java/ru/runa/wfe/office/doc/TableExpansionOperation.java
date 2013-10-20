package ru.runa.wfe.office.doc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.var.IVariableProvider;

import com.google.common.collect.Lists;

public class TableExpansionOperation extends Operation {
    private int rows = 0;
    private final List<ColumnExpansionOperation> operations;
    private List<Comparable> sortedMapKeys = null;

    public TableExpansionOperation(int columns) {
        this.operations = Lists.newArrayListWithExpectedSize(columns);
    }

    public int getRows() {
        return rows;
    }

    @Override
    public boolean isValid() {
        return rows > 0;
    }

    public void addOperation(int columnIndex, ColumnExpansionOperation operation) {
        while (columnIndex > operations.size()) {
            operations.add(null);
        }
        operations.add(columnIndex, operation);
        if (operation.getContainerValue() instanceof Map) {
            Map<Comparable<?>, ?> map = (Map<Comparable<?>, ?>) operation.getContainerValue();
            if (sortedMapKeys == null) {
                rows = map.size();
                sortedMapKeys = new ArrayList(map.keySet());
                Collections.sort(sortedMapKeys);
            }
        }
        if (operation.getContainerValue() instanceof List) {
            List<?> list = (List<?>) operation.getContainerValue();
            if (list.size() > rows) {
                rows = list.size();
            }
        }
    }

    public String getStringValue(DocxConfig config, IVariableProvider variableProvider, int columnIndex, int rowIndex) {
        ColumnExpansionOperation operation = operations.get(columnIndex);
        if (operation != null) {
            Object key;
            if (operation.getContainerValue() instanceof Map) {
                key = sortedMapKeys.get(rowIndex);
            } else {
                key = rowIndex;
            }
            return operation.getStringValue(config, variableProvider, key);
        }
        return null;
    }

    public List<Comparable> getSortedMapKeys() {
        return sortedMapKeys;
    }

}