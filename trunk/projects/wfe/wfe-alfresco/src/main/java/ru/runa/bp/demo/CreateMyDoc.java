package ru.runa.bp.demo;

import ru.runa.alfresco.AlfSession;
import ru.runa.bp.AlfHandler;
import ru.runa.bp.AlfHandlerData;
import ru.runa.wfe.var.FileVariable;

public class CreateMyDoc extends AlfHandler {

    @Override
    protected void executeAction(AlfSession session, AlfHandlerData alfHandlerData) throws Exception {
        MyDoc myDoc = new MyDoc();
        session.createObject(myDoc);
        FileVariable var = alfHandlerData.getInputParam(FileVariable.class, "file");
        session.setContent(myDoc, var.getData(), var.getContentType());
        alfHandlerData.setOutputParam("uuid", myDoc.getUuidRef());
    }

}
