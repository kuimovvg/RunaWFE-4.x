package ru.runa.bp.demo;

import ru.runa.alfresco.AlfSession;
import ru.runa.bp.AlfHandler;
import ru.runa.bp.AlfHandlerData;

public class RejectMyDoc extends AlfHandler {

    @Override
    protected void executeAction(AlfSession session, AlfHandlerData alfHandlerData) throws Exception {
        MyDoc myDoc = (MyDoc) session.loadObject(alfHandlerData.getInputParam(String.class, "uuid"));
        myDoc.setStatus(MyDoc.STATUS_REJECTED);
        session.updateObject(myDoc, false, "business process update");
    }
}
