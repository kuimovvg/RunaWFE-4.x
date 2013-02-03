package ru.runa.wfe.handler.action.var;

import java.util.ArrayList;

import ru.runa.wfe.handler.CommonParamBasedHandler;
import ru.runa.wfe.handler.HandlerData;

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
        handlerData.setOutputVariable("list", list);
        log.debug("Object " + object + " set to the list " + list + " at index " + index);
    }

}
