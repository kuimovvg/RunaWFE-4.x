package ru.runa.wf.var;

import java.util.Collections;
import java.util.List;

import ru.runa.wf.ParamBasedActionHandler;

public class SortListActionHandler extends ParamBasedActionHandler {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    @Override
    protected void executeAction() throws Exception {
        List list = getInputParam(List.class, "list", null);
        String mode = getInputParam(String.class, "mode");
        Collections.sort(list);
        if ("desc".equals(mode)) {
            Collections.reverse(list);
        }
        setOutputVariable("list", list);
        log.debug("Sorted [" + mode + "] list " + list);
    }

}
