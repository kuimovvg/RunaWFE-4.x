package ru.runa.wf.var;

import java.util.Collection;
import java.util.List;

import ru.runa.wf.ParamBasedActionHandler;

public class RemoveObjectFromListActionHandler extends ParamBasedActionHandler {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    @Override
    protected void executeAction() throws Exception {
        List list = getInputParam(List.class, "list");
        Object object = getInputParam(Object.class, "object");
        if (object instanceof Collection) {
            list.removeAll((Collection) object);
        } else {
            list.remove(object);
        }
        setOutputVariable("list", list);
        log.debug("Object " + object + " removed from the list " + list);
    }

}
