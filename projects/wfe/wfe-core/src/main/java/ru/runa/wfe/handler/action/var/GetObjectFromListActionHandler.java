package ru.runa.wfe.handler.action.var;

import java.util.List;

import ru.runa.wfe.handler.action.ParamBasedActionHandler;

import com.google.common.collect.Lists;

public class GetObjectFromListActionHandler extends ParamBasedActionHandler {

    @Override
    protected void executeAction() throws Exception {
        List<?> list = getInputParam(List.class, "list", null);
        if (list == null) {
            list = Lists.newArrayList();
        }
        int index = getInputParam(int.class, "index");
        Object object;
        if (list.size() > index) {
            object = list.get(index);
        } else {
            object = null;
        }
        setOutputVariable("object", object);
    }

}
