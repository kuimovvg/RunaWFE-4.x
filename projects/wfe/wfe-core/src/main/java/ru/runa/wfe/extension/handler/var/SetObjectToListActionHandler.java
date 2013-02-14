package ru.runa.wfe.extension.handler.var;

import java.util.ArrayList;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;

public class SetObjectToListActionHandler extends CommonParamBasedHandler {

    @SuppressWarnings("unchecked")
    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        ArrayList list = handlerData.getInputParam(ArrayList.class, "list", null);
        Object object = handlerData.getInputParam(Object.class, "object");
        int index = handlerData.getInputParam(int.class, "index");
        if (list == null) {
            list = new ArrayList();
        }
        for (int i = list.size(); i <= index; i++) {
            list.add(null);
        }
        list.set(index, object);
        handlerData.setOutputParam("list", list);
        log.debug("Object " + object + " set to the list " + list + " at index " + index);
    }

}
