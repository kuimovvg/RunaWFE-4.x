package ru.runa.wfe.var.format;

public interface VariableFormat {

    public Object parse(String[] source) throws Exception;

    public String format(Object object);
}
