package ru.runa.wfe.office.storage;

public class ConditionItem {

    private Op operator;
    private Object value;

    public ConditionItem() {
    }

    public ConditionItem(Op operator, Object value) {
        this.operator = operator;
        this.value = value;
    }

    public Op getOperator() {
        return operator;
    }

    public void setOperator(Op operator) {
        this.operator = operator;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
