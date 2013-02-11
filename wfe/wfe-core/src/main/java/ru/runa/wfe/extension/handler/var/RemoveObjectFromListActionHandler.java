package ru.runa.wfe.extension.handler.var;

import java.util.Collection;
import java.util.List;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;

public class RemoveObjectFromListActionHandler extends CommonParamBasedHandler {

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        List<?> list = handlerData.getInputParam(List.class, "list");
        Object object = handlerData.getInputParam(Object.class, "object");
        if (object instanceof Collection) {
            list.removeAll((Collection<?>) object);
        } else {
            list.remove(object);
        }
        handlerData.setOutputVariable("list", list);
        log.debug("Object " + object + " removed from the list " + list);
    }

}
