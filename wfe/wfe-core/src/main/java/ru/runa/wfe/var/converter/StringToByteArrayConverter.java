package ru.runa.wfe.var.converter;

import ru.runa.wfe.var.Converter;

public class StringToByteArrayConverter implements Converter {
    private static final long serialVersionUID = 1L;

    @Override
    public Object convert(Object o) {
        return ((String) o).getBytes();
    }

    @Override
    public Object revert(Object o) {
        return new String((byte[]) o);
    }

    @Override
    public boolean supports(Object value) {
        if (value == null) {
            return true;
        }
        return value.getClass().equals(String.class);
    }

}
