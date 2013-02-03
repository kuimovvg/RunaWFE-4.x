package ru.runa.wfe.handler.action.var;

import java.util.List;

import ru.runa.wfe.handler.CommonParamBasedHandler;
import ru.runa.wfe.handler.HandlerData;

import com.google.common.collect.Lists;

public class GetObjectFromListActionHandler extends CommonParamBasedHandler {

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        List<?> list = handlerData.getInputParam(List.class, "list", null);
        if (list == null) {
            list = Lists.newArrayList();
        }
        int index = handlerData.getInputParam(int.class, "index");
        Object object;
        if (list.size() > index) {
            object = list.get(index);
        } else {
            object = null;
        }
        handlerData.setOutputVariable("object", object);
    }

}
