package ru.runa.wfe.office.storage.binding;

import java.util.List;

import ru.runa.wfe.office.excel.IExcelConstraints;
import ru.runa.wfe.office.storage.ConditionItem;

public class DataBinding {
    private IExcelConstraints constraints;
    private String variableName;
    private List<ConditionItem> conditions;

    public IExcelConstraints getConstraints() {
        return constraints;
    }

    public void setConstraints(IExcelConstraints constraints) {
        this.constraints = constraints;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public List<ConditionItem> getConditions() {
        return conditions;
    }

    public void setConditions(List<ConditionItem> conditions) {
        this.conditions = conditions;
    }
}
