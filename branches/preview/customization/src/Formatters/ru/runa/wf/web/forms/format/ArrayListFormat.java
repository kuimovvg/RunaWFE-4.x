package ru.runa.wf.web.forms.format;

import java.util.ArrayList;
import java.util.Arrays;

import ru.runa.commons.format.WebFormat;

public class ArrayListFormat implements WebFormat {

    @Override
    public Object parse(String[] source) {
        // TODO tmp
        if (source != null && source.length > 0 && "new".equals(source[0])) {
            return new ArrayList<Object>();
        }
        return Arrays.asList(source);
    }

    @Override
    public String format(Object obj) {
        return obj.toString();
    }

}
