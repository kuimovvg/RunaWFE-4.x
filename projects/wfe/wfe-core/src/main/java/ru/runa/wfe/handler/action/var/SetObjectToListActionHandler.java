package ru.runa.wfe.handler.action.var;

import java.util.ArrayList;

import ru.runa.wfe.handler.action.ParamBasedActionHandler;

public class SetObjectToListActionHandler extends ParamBasedActionHandler {

    @SuppressWarnings("unchecked")
    @Override
    protected void executeAction() throws Exception {
        ArrayList list = getInputParam(ArrayList.class, "list", null);
        Object object = getInputParam(Object.class, "object");
        int index = getInputParam(int.class, "index");
        if (list == null) {
            list = new ArrayList();
        }
        for (int i = list.size(); i <= index; i++) {
            list.add(null);
        }
        list.set(index, object);
        setOutputVariable("list", list);
        log.debug("Object " + object + " set to the list " + list + " at index " + index);
    }

}
