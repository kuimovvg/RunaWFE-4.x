package ru.runa.bpm.ui.infopath;

import java.util.HashMap;
import java.util.Map;

import ru.runa.bpm.web.formgen.format.DoubleFormat;

import ru.runa.wf.web.forms.format.BooleanFormat;
import ru.runa.wf.web.forms.format.DateFormat;
import ru.runa.wf.web.forms.format.DateTimeFormat;
import ru.runa.wf.web.forms.format.FileFormat;
import ru.runa.wf.web.forms.format.LongFormat;
import ru.runa.wf.web.forms.format.StringFormat;
import ru.runa.wf.web.forms.format.TimeFormat;

public class TypeMapper {
    private static Map<String, String> map = new HashMap<String, String>();
    static {
        map.put("xsd:string", StringFormat.class.getName());
        map.put("xsd:boolean", BooleanFormat.class.getName());
        map.put("xsd:integer", LongFormat.class.getName());
        map.put("xsd:double", DoubleFormat.class.getName());
        map.put("xsd:date", DateFormat.class.getName());
        map.put("xsd:time", TimeFormat.class.getName());
        map.put("xsd:dateTime", DateTimeFormat.class.getName());
        map.put("my:requiredString", StringFormat.class.getName());
        map.put("xsd:base64Binary", FileFormat.class.getName());
        map.put("my:requiredBase64Binary", FileFormat.class.getName());
    }

    public static String getWfeTypeForInfopathType(String infoPathType) {
        String wfeFormat = map.get(infoPathType);
        if (wfeFormat == null) {
            throw new RuntimeException("No format mapping found for InfoPath type: " + infoPathType);
        }
        return wfeFormat;
    }
}
