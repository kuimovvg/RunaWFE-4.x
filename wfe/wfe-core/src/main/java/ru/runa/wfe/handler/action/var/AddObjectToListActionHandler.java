package ru.runa.wfe.handler.action.var;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ru.runa.wfe.handler.CommonParamBasedHandler;
import ru.runa.wfe.handler.HandlerData;

public class AddObjectToListActionHandler extends CommonParamBasedHandler {

    @SuppressWarnings("rawtypes")
    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        List list = handlerData.getInputParam(List.class, "list", null);
        if (list == null) {
            list = new ArrayList();
        }
        Object object = handlerData.getInputParam(Object.class, "object");
        if (object instanceof Collection) {
            list.addAll((Collection) object);
        } else {
            list.add(object);
        }
        handlerData.setOutputVariable("list", list);
        log.debug("Object " + object + " added to the list " + list);
    }

}
