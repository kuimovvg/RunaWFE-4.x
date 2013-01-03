package ru.runa.wf.web.ftl.method;

import java.util.Date;

import javax.security.auth.Subject;

import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.var.FileVariable;
import ru.runa.wfe.var.ISelectable;
import ru.runa.wfe.var.format.FileFormat;

public class ViewUtil {

    public static String getVarOut(Object object, Subject subject, WebHelper webHelper, Long instanceId, String name, int listIndex, Object mapKey) {
        String value;
        if (object instanceof ISelectable) {
            value = ((ISelectable) object).getDisplayName();
        } else if (object instanceof Date) {
            value = CalendarUtil.formatDate((Date) object);
        } else if (object instanceof FileVariable) {
            value = FileFormat.getHtml((FileVariable) object, subject, webHelper, instanceId, name, listIndex, mapKey);
        } else if (object == null) {
            value = "";
        } else {
            value = String.valueOf(object);
        }
        return value;
    }
}
