package ru.runa.wfe.handler.action.var;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ru.runa.wfe.handler.action.ParamBasedActionHandler;

public class AddObjectToListActionHandler extends ParamBasedActionHandler {

    @SuppressWarnings("rawtypes")
    @Override
    protected void executeAction() throws Exception {
        List list = getInputParam(List.class, "list", null);
        if (list == null) {
            list = new ArrayList();
        }
        Object object = getInputParam(Object.class, "object");
        if (object instanceof Collection) {
            list.addAll((Collection) object);
        } else {
            list.add(object);
        }
        setOutputVariable("list", list);
        log.debug("Object " + object + " added to the list " + list);
    }

}
