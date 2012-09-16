package ru.runa.bpm.ui.util;

import org.eclipse.jface.viewers.ICellEditorValidator;

public class NumberCellEditorValidator implements ICellEditorValidator {

    public String isValid(Object value) {
        try {
            if (value instanceof String) {
                new Long((String) value);
                return null;
            }
            return "error";
        } catch (Exception e) {
            return "error";
        }
    }

}
