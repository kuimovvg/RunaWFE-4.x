package ru.runa.bpm.context.exe.matcher;

import org.springframework.beans.factory.annotation.Required;

import ru.runa.bpm.context.exe.JbpmTypeMatcher;
import ru.runa.bpm.context.exe.variableinstance.StringInstance;

public class StringMatcher implements JbpmTypeMatcher {
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
            return len >= StringInstance.MAX_STRING_SIZE;
        }
        return len < StringInstance.MAX_STRING_SIZE;
    }

}
