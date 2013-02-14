package ru.runa.wfe.extension.handler.var;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;

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
        handlerData.setOutputParam("list", list);
        log.debug("Object " + object + " added to the list " + list);
    }

}
