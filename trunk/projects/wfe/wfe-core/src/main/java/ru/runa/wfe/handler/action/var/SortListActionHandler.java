package ru.runa.wfe.handler.action.var;

import java.util.Collections;
import java.util.List;

import ru.runa.wfe.handler.action.ParamBasedActionHandler;

public class SortListActionHandler extends ParamBasedActionHandler {

    @SuppressWarnings("rawtypes")
    @Override
    protected void executeAction() throws Exception {
        List<Comparable> list = getInputParam(List.class, "list", null);
        String mode = getInputParam(String.class, "mode");
        Collections.sort(list);
        if ("desc".equals(mode)) {
            Collections.reverse(list);
        }
        setOutputVariable("list", list);
        log.debug("Sorted [" + mode + "] list " + list);
    }

}
