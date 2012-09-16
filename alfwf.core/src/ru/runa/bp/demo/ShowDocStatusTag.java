package ru.runa.bp.demo;

import ru.runa.alfresco.AlfSession;
import ru.runa.bp.AlfAjaxTag;

public class ShowDocStatusTag extends AlfAjaxTag {

    @Override
    protected String renderRequest(AlfSession session) throws Exception {
        String uuid = getVariableAs(String.class, "uuid", false);
        MyDoc myDoc = (MyDoc) session.loadObject(uuid);
        return "<b>" + myDoc.getStatus() + "</b>";
    }

}
