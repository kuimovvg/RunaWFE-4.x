package ru.runa.bp.demo;

import ru.runa.alfresco.RemoteAlfConnection;
import ru.runa.bp.AlfHandler;
import ru.runa.bp.AlfHandlerData;

public class RejectMyDoc extends AlfHandler {

    @Override
    protected void executeAction(RemoteAlfConnection session, AlfHandlerData alfHandlerData) throws Exception {
        MyDoc myDoc = session.loadObjectNotNull(alfHandlerData.getInputParamValueNotNull(String.class, "uuid"));
        myDoc.setStatus(MyDoc.STATUS_REJECTED);
        session.updateObject(myDoc, false, "business process update");
    }
}
