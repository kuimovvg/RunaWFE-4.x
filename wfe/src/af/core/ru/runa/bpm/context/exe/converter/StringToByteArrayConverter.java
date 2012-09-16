package ru.runa.bpm.context.exe.converter;

import ru.runa.bpm.context.exe.Converter;

public class StringToByteArrayConverter implements Converter {

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
