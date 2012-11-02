package ru.runa.wfe.handler.action.var;

import java.util.Collection;
import java.util.List;

import ru.runa.wfe.handler.action.ParamBasedActionHandler;

public class RemoveObjectFromListActionHandler extends ParamBasedActionHandler {

    @Override
    protected void executeAction() throws Exception {
        List<?> list = getInputParam(List.class, "list");
        Object object = getInputParam(Object.class, "object");
        if (object instanceof Collection) {
            list.removeAll((Collection<?>) object);
        } else {
            list.remove(object);
        }
        setOutputVariable("list", list);
        log.debug("Object " + object + " removed from the list " + list);
    }

}
