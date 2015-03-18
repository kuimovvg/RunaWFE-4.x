package ru.runa.gpd.office.store;

public class UpdateConditionItem extends ConditionItem {

    private Object newValue;

    public UpdateConditionItem() {
    }

    public UpdateConditionItem(Op condition, Object value, Object newValue) {
        super(condition, value);
        this.newValue = newValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }

    @Override
    public String toString() {
        return "UpdateConditionItem [newValue=" + newValue + "]";
    }
}
