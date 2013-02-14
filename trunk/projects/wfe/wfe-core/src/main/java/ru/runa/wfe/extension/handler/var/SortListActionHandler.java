package ru.runa.wfe.extension.handler.var;

import java.util.Collections;
import java.util.List;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;

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
        handlerData.setOutputParam("list", list);
        log.debug("Sorted [" + mode + "] list " + list);
    }

}
