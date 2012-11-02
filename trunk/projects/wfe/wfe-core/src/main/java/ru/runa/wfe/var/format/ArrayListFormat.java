package ru.runa.wfe.var.format;

import java.util.Arrays;


public class ArrayListFormat implements VariableFormat {

    @Override
    public Object parse(String[] source) {
        return Arrays.asList(source);
    }

    @Override
    public String format(Object obj) {
        return obj.toString();
    }

}
