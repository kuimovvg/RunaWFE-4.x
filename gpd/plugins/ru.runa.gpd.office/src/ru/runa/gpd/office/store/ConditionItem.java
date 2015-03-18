package ru.runa.gpd.office.store;

public class ConditionItem {
    private Op condition;
    private Object value;

    public ConditionItem() {
    }

    public ConditionItem(Op condition, Object value) {
        this.condition = condition;
        this.value = value;
    }

    public Op getCondition() {
        return condition;
    }

    public void setCondition(Op condition) {
        this.condition = condition;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((condition == null) ? 0 : condition.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ConditionItem other = (ConditionItem) obj;
        if (condition != other.condition) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ConditionItem [condition=" + condition + ", value=" + value + "]";
    }

}
