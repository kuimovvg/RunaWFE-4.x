package ru.runa.commons.format;

public interface WebFormat {

    public Object parse(String[] source) throws Exception;

    public String format(Object object);
}
