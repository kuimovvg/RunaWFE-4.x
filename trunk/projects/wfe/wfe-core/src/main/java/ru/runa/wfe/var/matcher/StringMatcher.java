package ru.runa.wfe.var.matcher;

import org.springframework.beans.factory.annotation.Required;

import ru.runa.wfe.var.VariableTypeMatcher;
import ru.runa.wfe.var.impl.StringVariable;

public class StringMatcher implements VariableTypeMatcher {
    private boolean large;
    
    @Required
    public void setLarge(boolean large) {
        this.large = large;
    }

    public boolean matches(Object value) {
        if (value.getClass() != String.class) {
            return false;
        }
        int len = ((String) value).length();
        if (large) {
            return len >= StringVariable.MAX_STRING_SIZE;
        }
        return len < StringVariable.MAX_STRING_SIZE;
    }

}
