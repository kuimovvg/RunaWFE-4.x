package ru.runa.bpm.context.exe.variableinstance;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import ru.runa.bpm.context.exe.VariableInstance;

@Entity
@DiscriminatorValue(value="N")
public class NullInstance extends VariableInstance<Object> {

    @Override
    public boolean isStorable(Object value) {
        return value == null;
    }

    @Override
    @Transient
    protected Object getObject() {
        return null;
    }

    @Override
    protected void setNewValue(Object value) {
    }
    
}
