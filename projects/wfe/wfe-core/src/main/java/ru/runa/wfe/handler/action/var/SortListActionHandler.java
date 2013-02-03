package ru.runa.wfe.handler.action.var;

import java.util.Collections;
import java.util.List;

import ru.runa.wfe.handler.CommonParamBasedHandler;
import ru.runa.wfe.handler.HandlerData;

public class SortListActionHandler extends CommonParamBasedHandler {

    @SuppressWarnings("rawtypes")
    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        List<Comparable> list = handlerData.getInputParam(List.class, "list", null);
        String mode = handlerData.getInputParam(String.class, "mode");
        Collections.sort(list);
        if ("desc".equals(mode)) {
            Collections.reverse(list);
        }
        handlerData.setOutputVariable("list", list);
        log.debug("Sorted [" + mode + "] list " + list);
    }

}
